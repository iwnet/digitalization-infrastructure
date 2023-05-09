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

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class ConsumerCreator {
    private static Configuration configuration;
    public static String OFFSET_RESET_LATEST="latest";
    public static String OFFSET_RESET_EARLIER="earliest";

    public static Consumer<Long, String> createConsumer() {
        configuration = Configuration.getInstance();

        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configuration.pubSubBackend.getBrokerUrl());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, configuration.pubSubBackend.getClientId());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, configuration.pubSubBackend.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OFFSET_RESET_EARLIER);
        //props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);
        //props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000);
        props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, configuration.pubSubBackend.getClientId());
        //props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, false);

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
        props.put("sasl.login.refresh.buffer.seconds", (short) 10);
        props.put("sasl.login.refresh.min.period.seconds", (short) 30);
        props.put("sasl.login.callback.handler.class", "com.bfm.kafka.security.oauthbearer.OAuthAuthenticateLoginCallbackHandler");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");


        final Consumer<Long, String> consumer = new KafkaConsumer<>(props);

        return consumer;
    }

}
