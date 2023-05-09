/*
 * Copyright 2022 ICCS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.ntua.ece.cslab.iwnet.bda.controller;

import gr.ntua.ece.cslab.iwnet.bda.common.Configuration;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class AuthClientBackend {

    private static Configuration configuration = Configuration.getInstance();

    public static String getAccessToken() {

        StringBuilder builder = new StringBuilder();
        builder.append(configuration.authClientBackend.getAuthServerUrl());
        builder.append("realms/master/protocol/openid-connect/token");
        String POST_URL = builder.toString();

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(POST_URL);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("client_id", "admin-cli"));
        params.add(new BasicNameValuePair("username", configuration.authClientBackend.getAdminUsername()));
        params.add(new BasicNameValuePair("password", configuration.authClientBackend.getAdminPassword()));
        params.add(new BasicNameValuePair("grant_type", "password"));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }

        //Execute and get the response.
        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        String results = null;
        try {
            results = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        JSONObject responseObject = new JSONObject(results);

        //System.out.println(responseObject);

        return (String) responseObject.get("access_token");
    }

    public static void createClientScope(String scope){
        String token = getAccessToken();

        StringBuilder builder = new StringBuilder();
        builder.append(configuration.authClientBackend.getAuthServerUrl());
        builder.append("admin/realms/");
        builder.append(configuration.authClientBackend.getRealm());
        builder.append("/client-scopes");
        String POST_URL = builder.toString();

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(POST_URL);

        httppost.setEntity(new StringEntity("{\"name\":\""+scope+"\", \"protocol\":\"openid-connect\"}", ContentType.APPLICATION_JSON));

        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-Type", "application/json");
        httppost.setHeader("Authorization", "bearer "+token);

        //Execute and get the response.
        HttpResponse response = null;
        try {
            response = httpclient.execute(httppost);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (response.getStatusLine().getStatusCode()!=201) {
            System.out.println("Failed to create client scope.");
            System.out.println(response.getStatusLine());
            return;
        }



        builder = new StringBuilder();
        builder.append(configuration.authClientBackend.getAuthServerUrl());
        builder.append("admin/realms/");
        builder.append(configuration.authClientBackend.getRealm());
        builder.append("/clients?clientId="+configuration.pubSubBackend.getClientId());
        String GET_URL = builder.toString();

        HttpGet httpget = new HttpGet(GET_URL);

        httpget.setHeader("Authorization", "bearer "+token);

        //Execute and get the response.
        response = null;
        try {
            response = httpclient.execute(httpget);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String results = null;
        try {
            results = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        JSONObject responseObject = new JSONArray(results).getJSONObject(0);
        String clientId = responseObject.getString("id");



        builder = new StringBuilder();
        builder.append(configuration.authClientBackend.getAuthServerUrl());
        builder.append("admin/realms/");
        builder.append(configuration.authClientBackend.getRealm());
        builder.append("/client-scopes");
        GET_URL = builder.toString();

        httpget = new HttpGet(GET_URL);

        httpget.setHeader("Authorization", "bearer "+token);

        //Execute and get the response.
        response = null;
        try {
            response = httpclient.execute(httpget);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        results = null;
        try {
            results = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String scopeId = null;
        JSONArray jsonArray = new JSONArray(results);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString("name").matches(scope)){
                scopeId = jsonObject.getString("id");
                break;
            }
        }



        builder = new StringBuilder();
        builder.append(configuration.authClientBackend.getAuthServerUrl());
        builder.append("admin/realms/");
        builder.append(configuration.authClientBackend.getRealm());
        builder.append("/clients/"+clientId);
        builder.append("/default-client-scopes/"+scopeId);
        String PUT_URL = builder.toString();

        HttpPut httpput = new HttpPut(PUT_URL);

        httpput.setHeader("Authorization", "bearer "+token);

        //Execute and get the response.
        response = null;
        try {
            response = httpclient.execute(httpput);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }


}
