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

import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.DbInfo;
import gr.ntua.ece.cslab.iwnet.bda.controller.AuthClientBackend;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.MessageType;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsumerApp {
    private final static Logger LOGGER = Logger.getLogger(ConsumerApp.class.getCanonicalName()+" [" + Thread.currentThread().getName() + "]");

    private KafkaConsumerRunner runner;

    private static ConsumerApp consumerApp;

    public ConsumerApp() { }

    public static ConsumerApp getInstance() {
        if (consumerApp == null){
            consumerApp = new ConsumerApp();
        }
        return consumerApp;
    }

    public static void init() {
        if (consumerApp == null) {
            consumerApp = new ConsumerApp();
        }
        List<String> messageTypes = new LinkedList<>();

        try {
            List<String> allMessageTypes;
            for (DbInfo dbInfo : DbInfo.getDbInfo()) {
                allMessageTypes = MessageType.getActiveMessageTypeNames(dbInfo.getSlug());
                if (!allMessageTypes.isEmpty())
                    messageTypes.addAll(allMessageTypes);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Failed to get message types. Aborting initialization of consumer.");
            return;
        }

        if (messageTypes.isEmpty()){
            return;
        }

        consumerApp.runner = new KafkaConsumerRunner();
        consumerApp.runner.messageTypes = messageTypes;
        Thread thread = new Thread(consumerApp.runner);
        thread.start();

    }

    public static class KafkaConsumerRunner implements Runnable {
        private final AtomicBoolean closed = new AtomicBoolean(false);
        private final Consumer consumer;
        private List<String> messageTypes;

        public KafkaConsumerRunner() {
            this.consumer = ConsumerCreator.createConsumer();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    consumer.subscribe(messageTypes);

                    while (!closed.get()) {
                        final ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofSeconds(10));
                        consumerRecords.forEach
                                (record -> {
                                    LOGGER.log(Level.INFO, record.value());
                                    try {
                                        PubSubMessageHandler.handleMessage(record.value());
                                        LOGGER.log(Level.INFO, "PubSub message successfully inserted in the BDA.");

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        LOGGER.log(Level.SEVERE, "Could not insert new PubSub message in HBase.");
                                    }
                                });
                        consumer.commitAsync();
                    }
                } catch (WakeupException e) {
                    if (closed.get())
                        break;
                } catch (TopicAuthorizationException e){
                    //LOGGER.log(Level.INFO, e.toString());
                } catch (Exception e){
                    e.printStackTrace();
                    LOGGER.log(Level.SEVERE, "Shutting down consumer.");
                    break;
                }
            }
            consumer.close();
        }

        public void resubscribe(){
            consumer.wakeup();
        }

        // Shutdown hook which can be called from a separate thread
        public void shutdown() {
            closed.set(true);
            consumer.wakeup();
        }
    }

    public void addTopic(String name) {
        TopicCreator.createTopic(name);
        AuthClientBackend.createClientScope("urn:kafka:topic:"+name+":read");
        AuthClientBackend.createClientScope("urn:kafka:topic:"+name+":describe");
        AuthClientBackend.createClientScope("urn:kafka:topic:"+name+":write");

        if (runner!=null) {
            runner.messageTypes.add(name);
            // Wait 2 minutes before resubscribing in order for the oauth token to be refreshed first
            new Thread(() -> {
                try {
                    Thread.sleep(120*1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ConsumerApp.getInstance().runner.resubscribe();
            }).start();
        }
        else {
            runner = new KafkaConsumerRunner();
            runner.messageTypes = new LinkedList<>();
            runner.messageTypes.add(name);
            Thread thread = new Thread(runner);
            thread.start();
        }

    }

    public void removeTopic(String name) {
        //TODO: destroy topic in broker and delete client scopes
        //TopicCreator.deleteTopic(name);
        //AuthClientBackend.deleteClientScope(..);

        runner.messageTypes.remove(name);

        if (!runner.messageTypes.isEmpty())
            runner.resubscribe();
        else {
            runner.shutdown();
            runner=null;
        }
    }

    public void close(){
        if (runner!=null)
            runner.shutdown();
    }


}
