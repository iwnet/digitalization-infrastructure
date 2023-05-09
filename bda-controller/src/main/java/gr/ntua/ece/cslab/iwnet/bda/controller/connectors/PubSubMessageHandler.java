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

package gr.ntua.ece.cslab.iwnet.bda.controller.connectors;

import gr.ntua.ece.cslab.iwnet.bda.analyticsml.RunnerInstance;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.DbInfo;
import gr.ntua.ece.cslab.iwnet.bda.datastore.StorageBackend;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.KeyValue;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Message;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.MessageType;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PubSubMessageHandler {
    private final static Logger LOGGER = Logger.getLogger(PubSubMessageHandler.class.getCanonicalName()+" [" + Thread.currentThread().getName() + "]");

    public static void handleMessage(String message) throws Exception {
        JSONObject msgJson = new JSONObject(StringEscapeUtils.unescapeJava(message.replaceAll("^\"|\"$", "")));

        if (!msgJson.has("slug")){
            LOGGER.log(Level.SEVERE,"Received message with no SCN slug.");
            throw new Exception("Could not insert new PubSub message. Missing SCN identifier.");
        }
        String slug = msgJson.getString("slug");
        if (slug.isEmpty()) {
            LOGGER.log(Level.SEVERE,"Received message with empty SCN slug.");
            throw new Exception("Could not insert new PubSub message. Empty SCN identifier.");
        }
        try {
            DbInfo.getDbInfoBySlug(slug);
        } catch (Exception e){
            LOGGER.log(Level.SEVERE,"Received message with invalid SCN slug.");
            throw new Exception("Could not insert new PubSub message. Invalid slug field.");
        }

        if (!msgJson.has("message_type")) {
            LOGGER.log(Level.SEVERE, "Received message with no message type.");
            throw new Exception("Could not insert new PubSub message. Missing message type.");
        }
        String messageType = msgJson.getString("message_type");
        if (messageType.isEmpty()) {
            LOGGER.log(Level.SEVERE,"Received message with empty messageType.");
            throw new Exception("Could not insert new PubSub message. Empty MessageType field.");
        }

        List<String> messageTypeNames;
        try {
            messageTypeNames = MessageType.getActiveMessageTypeNames(slug);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not get registered message types to validate incoming message.");
            throw e;
        }
        if (!messageTypeNames.contains(messageType)){
            LOGGER.log(Level.SEVERE, "Received invalid message type: " + messageType + " for slug: "+slug+".");
            throw new Exception("Could not insert new PubSub message. Invalid message type.");
        }

        if (!msgJson.has("payload")){
            LOGGER.log(Level.SEVERE,"Received message with no payload.");
            throw new Exception("Could not insert new PubSub message. Missing payload.");
        }
        String payload = msgJson.get("payload").toString();
        if (payload.isEmpty()){
            LOGGER.log(Level.SEVERE,"Received message with empty payload.");
            throw new Exception("Could not insert new PubSub message. Empty payload.");
        }

        Message msg = new Message();
        msg.setNested(Collections.emptyList());
        List<KeyValue> entries = new LinkedList<>();
        for (String key: msgJson.keySet()) {
            KeyValue kv = new KeyValue(key, msgJson.get(key).toString());
            entries.add(kv);
        }
        msg.setEntries(entries);
        String messageId = new StorageBackend(slug).insert(msg);

        try {
            (new RunnerInstance(slug, messageType)).run(messageId);
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Did not launch job. "+e.getMessage());
        }
    }
}
