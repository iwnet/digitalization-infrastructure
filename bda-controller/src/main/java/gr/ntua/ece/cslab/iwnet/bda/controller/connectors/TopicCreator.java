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

import gr.ntua.ece.cslab.iwnet.bda.common.Configuration;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.common.errors.TopicExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopicCreator {
    private static Configuration configuration;

    private final static Logger LOGGER = Logger.getLogger(TopicCreator.class.getCanonicalName()+" [" + Thread.currentThread().getName() + "]");

    public static void createTopic(String topic) {
        configuration = Configuration.getInstance();

        final Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.pubSubBackend.getBrokerUrl());
        props.put(AdminClientConfig.CLIENT_ID_CONFIG, configuration.pubSubBackend.getClientId());

        // OAuth and SSL Settings
        if (configuration.pubSubBackend.isSSLEnabled()) {
            props.put("security.protocol", "SASL_SSL");
            props.put("ssl.truststore.location", configuration.pubSubBackend.getConsumerSSLTruststoreLocation());
            props.put("ssl.truststore.password", configuration.pubSubBackend.getConsumerSSLTruststorePassword());
            props.put("ssl.keystore.location", configuration.pubSubBackend.getConsumerSSLKeystoreLocation());
            props.put("ssl.keystore.password", configuration.pubSubBackend.getConsumerSSLKeystorePassword());
            props.put("ssl.key.password", configuration.pubSubBackend.getConsumerSSLKeyPassword());

            //props.put("enabled.protocols", "TLSv1.2,TLSv1.1,TLSv1");
            props.put("ssl.endpoint.identification.algorithm", "");
        } else
            props.put("security.protocol", "SASL_PLAINTEXT");
        props.put("sasl.mechanism", "OAUTHBEARER");
        props.put("sasl.login.callback.handler.class", "com.bfm.kafka.security.oauthbearer.OAuthAuthenticateLoginCallbackHandler");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");


        AdminClient adminClient = AdminClient.create(props);
        NewTopic newTopic = new NewTopic(topic, configuration.pubSubBackend.getNumPartitions(), (short) configuration.pubSubBackend.getReplicationFactor());

        List<NewTopic> newTopics = new ArrayList<>();
        newTopics.add(newTopic);

        try {
            CreateTopicsResult res = adminClient.createTopics(newTopics);
            res.all().get();
        } catch (ExecutionException e){
            if (e.getCause() != null && e.getCause() instanceof TopicExistsException) {
                LOGGER.log(Level.INFO, "topic exists: {}", topic);
            } else {
                LOGGER.log(Level.SEVERE, "Topic create failed with: {}", e);
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e){
            LOGGER.log(Level.SEVERE, "Topic create failed with: {}", e.getMessage());
            throw new RuntimeException(e);
        } 
        adminClient.close();

    }

}
