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

package gr.ntua.ece.cslab.iwnet.bda.common;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;



import java.lang.IllegalStateException;



/**
 * This class holds all the configuration options used by the Big Data Analytics component as a whole
 * (including the various subsystems).
 */
public class Configuration {
    private static Logger LOGGER =Logger.getLogger(Configuration.class.getCanonicalName());

    private static Configuration configuration = null;

    public final StorageBackend storageBackend;
    public final ExecutionEngine execEngine;
    public final PubSubBackend pubSubBackend;
    public final AuthClientBackend authClientBackend;
    public final KPIBackend kpiBackend;

    public class StorageBackend {
        // TODO: Should add username/password for every StorageBackend.
        //       Modify Constructor accordingly.
        private String eventLogURL, dimensionTablesURL, bdaDatabaseURL, dbUsername, dbPassword;
        private String dbPrivilegedUsername, dbPrivilegedPassword;

        private String eventLogMaster = null;
        private String eventLogQuorum = null;

        private String hdfsMasterURL;
        private String hdfsUsername;
        private String hdfsPassword;

        public StorageBackend() {
        }

        public String getEventLogURL() {
            return eventLogURL;
        }

        public String getDimensionTablesURL() {
            return dimensionTablesURL;
        }

        public String getBdaDatabaseURL() {
            return bdaDatabaseURL;
        }

        public String getDbUsername() { return dbUsername; }

        public String getDbPassword() { return dbPassword; }

        public String getDbPrivilegedUsername() { return dbPrivilegedUsername; }

        public String getDbPrivilegedPassword() { return dbPrivilegedPassword; }

        public String getEventLogMaster() { return eventLogMaster; }

        public String getEventLogQuorum() { return eventLogQuorum; }

        public String getHDFSMasterURL() { return hdfsMasterURL; }

        public String getHDFSUsername() { return hdfsUsername; }

        public String getHDFSPassword() { return hdfsPassword; }

    }
    public class ExecutionEngine {
        private String sparkMaster;
        private String sparkDeployMode;
        private String sparkConfJars;
        private String sparkConfDriverMemory;
        private String sparkConfExecutorCores;
        private String sparkConfExecutorMemory;

        private String recipeStorageLocation;
        private String recipeStorageType;

        private String livyURL;

        public ExecutionEngine(){}

        public String getSparkMaster() { return sparkMaster; }

        public String getSparkDeployMode() { return sparkDeployMode; }

        public String getSparkConfJars() { return sparkConfJars; }

        public String getSparkConfDriverMemory() { return sparkConfDriverMemory; }

        public String getSparkConfExecutorCores() { return sparkConfExecutorCores; }

        public String getSparkConfExecutorMemory() { return sparkConfExecutorMemory; }

        public String getRecipeStorageLocation() { return recipeStorageLocation; }

        public String getRecipeStorageType() { return recipeStorageType; }

        public String getLivyURL() { return livyURL; }
    }

    public class PubSubBackend {
        private String brokerUrl, authServerUri, clientId, clientSecret, groupId, consumerSSLTruststoreLocation,
                consumerSSLTruststorePassword, consumerSSLKeystoreLocation, consumerSSLKeystorePassword,
                consumerSSLKeyPassword, producerSSLCALocation, producerSSLCertificateLocation, producerkeyLocation;
        private Boolean SSLEnabled;
        private int numPartitions, replicationFactor;

        public PubSubBackend(){ }

        public String getBrokerUrl() { return brokerUrl; }
	
        public String getAuthServerUri() { return authServerUri; }

        public String getClientId() { return clientId; }

        public String getClientSecret() { return clientSecret; }

        public int getNumPartitions() { return numPartitions; }

        public int getReplicationFactor() { return replicationFactor; }

        public String getGroupId() { return groupId; }

        public Boolean isSSLEnabled() { return SSLEnabled; }

        public String getConsumerSSLTruststoreLocation() { return consumerSSLTruststoreLocation; }

        public String getConsumerSSLTruststorePassword() { return consumerSSLTruststorePassword; }

        public String getConsumerSSLKeystoreLocation() { return consumerSSLKeystoreLocation; }

        public String getConsumerSSLKeystorePassword() { return consumerSSLKeystorePassword; }

        public String getConsumerSSLKeyPassword() { return consumerSSLKeyPassword; }

        public String getProducerSSLCALocation() { return producerSSLCALocation; }

        public String getProducerSSLCertificateLocation() { return producerSSLCertificateLocation; }

        public String getProducerkeyLocation() { return producerkeyLocation; }

    }


    public class AuthClientBackend {
        private Boolean authEnabled;
        private String authServerUrl, realm, adminUsername, adminPassword;

        public AuthClientBackend() { }

        public Boolean isAuthEnabled() { return authEnabled; }

        public String getAuthServerUrl() { return authServerUrl; }

        public String getRealm() { return realm; }

        public String getAdminUsername() { return adminUsername; }

        public String getAdminPassword() { return adminPassword; }

    }

    public class KPIBackend {
        private String dbUrl, dbUsername, dbPassword;

        public String getDbUrl() { return dbUrl; }

        public String getDbUsername() { return dbUsername; }

        public String getDbPassword() { return dbPassword; }

    }

    public static Configuration getInstance() throws IllegalStateException {
        if (configuration == null) {
            throw new IllegalStateException("Configuration not initialized.");
        }

        return configuration;
    }

    private Configuration() {
        this.storageBackend = new StorageBackend();
        this.authClientBackend = new AuthClientBackend();
        this.kpiBackend = new KPIBackend();
        this.execEngine = new ExecutionEngine();
        this.pubSubBackend = new PubSubBackend();
    }

    /**
     * parseConfiguration constructs a Configuration object through reading the provided configuration file.
     * @param configurationFile the path of the configuration file
     * @return the Configuration object
     */
    public static Configuration parseConfiguration(String configurationFile){
        Properties properties = new Properties();
        try {
            properties.load(new FileReader(configurationFile));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            return null;
        }

        Configuration conf = new Configuration();

        // Dimension Tables Configuration.
        conf.storageBackend.dbUsername = properties.getProperty("backend.db.dimension.username");
        conf.storageBackend.dbPassword = properties.getProperty("backend.db.dimension.password");
        conf.storageBackend.dbPrivilegedUsername = properties.getProperty("backend.db.dimension.privileged_username");
        conf.storageBackend.dbPrivilegedPassword = properties.getProperty("backend.db.dimension.privileged_password");
        conf.storageBackend.dimensionTablesURL = properties.getProperty("backend.db.dimension.url");

        // BDA Database Configuration.

        // TODO: Should add username/password for every StorageBackend.
        // conf.storageBackend.bdaDatabaseUsername = properties.getProperty("backend.db.bda.username");
        // conf.storageBackend.bdaDatabasePassword = properties.getProperty("backend.db.bda.password");

        conf.storageBackend.bdaDatabaseURL = properties.getProperty("backend.db.bda.url");

        // Event Log Configuration.

        // TODO: Should add username/password for every StorageBackend.
        // conf.storageBackend.eventLogUsername = properties.getProperty("backend.db.event.username");
        // conf.storageBackend.eventLogPassword = properties.getProperty("backend.db.event.password");

        conf.storageBackend.eventLogURL = properties.getProperty("backend.db.event.url");
        conf.storageBackend.eventLogMaster = properties.getProperty("backend.db.event.master.host");
        conf.storageBackend.eventLogQuorum = properties.getProperty("backend.db.event.quorum");

        conf.storageBackend.hdfsMasterURL = properties.getProperty("backend.hdfs.master.url");

        // Kafka Configuration.
        conf.pubSubBackend.brokerUrl = properties.getProperty("kafka.broker.url");
        conf.pubSubBackend.SSLEnabled = Boolean.valueOf(properties.getProperty("kafka.ssl.enabled"));
        conf.pubSubBackend.authServerUri = properties.getProperty("oauth.server.base.uri");
        conf.pubSubBackend.clientId = properties.getProperty("oauth.server.client.id");
        conf.pubSubBackend.clientSecret = properties.getProperty("oauth.server.client.secret");
        conf.pubSubBackend.groupId = properties.getProperty("kafka.consumer.group.id");
        conf.pubSubBackend.numPartitions = Integer.parseInt(properties.getProperty("kafka.consumer.topic.numPartitions"));
        conf.pubSubBackend.replicationFactor = Integer.parseInt(properties.getProperty("kafka.consumer.topic.replicationFactor"));
        conf.pubSubBackend.consumerSSLKeystoreLocation = properties.getProperty("kafka.consumer.ssl.keystoreLocation");
        conf.pubSubBackend.consumerSSLKeystorePassword = properties.getProperty("kafka.consumer.ssl.keystorePassword");
        conf.pubSubBackend.consumerSSLTruststoreLocation = properties.getProperty("kafka.consumer.ssl.truststoreLocation");
        conf.pubSubBackend.consumerSSLTruststorePassword = properties.getProperty("kafka.consumer.ssl.truststorePassword");
        conf.pubSubBackend.consumerSSLKeyPassword = properties.getProperty("kafka.consumer.ssl.keyPassword");
        conf.pubSubBackend.producerSSLCALocation = properties.getProperty("kafka.producer.ssl.caLocation");
        conf.pubSubBackend.producerSSLCertificateLocation = properties.getProperty("kafka.producer.ssl.certificateLocation");
        conf.pubSubBackend.producerkeyLocation = properties.getProperty("kafka.producer.ssl.keyLocation");


        // Keycloak Auth Configuration.
        conf.authClientBackend.authEnabled = Boolean.valueOf(properties.getProperty("keycloak.enabled"));
        conf.authClientBackend.authServerUrl = properties.getProperty("keycloak.auth-server-url");
        conf.authClientBackend.realm = properties.getProperty("keycloak.realm");
        conf.authClientBackend.adminUsername = properties.getProperty("server.keycloak.admin.username");
        conf.authClientBackend.adminPassword = properties.getProperty("server.keycloak.admin.password");


        // KPIDB configuration
        conf.kpiBackend.dbUrl = properties.getProperty("kpi.db.url");
        conf.kpiBackend.dbUsername = properties.getProperty("kpi.db.username");
        conf.kpiBackend.dbPassword = properties.getProperty("kpi.db.password");

        // Execution engine configuration
        conf.execEngine.sparkMaster = properties.getProperty("spark.master");
        conf.execEngine.sparkDeployMode = properties.getProperty("spark.deploy_mode");
        conf.execEngine.sparkConfJars = properties.getProperty("spark.conf.jars");
        conf.execEngine.sparkConfDriverMemory = properties.getProperty("spark.conf.driver_memory");
        conf.execEngine.sparkConfExecutorCores = properties.getProperty("spark.conf.executor_cores");
        conf.execEngine.sparkConfExecutorMemory = properties.getProperty("spark.conf.executor_memory");
        conf.execEngine.recipeStorageLocation = properties.getProperty("engines.recipe.storage.prefix");
        conf.execEngine.recipeStorageType = properties.getProperty("engines.recipe.storage.type");
        conf.execEngine.livyURL = properties.getProperty("spark.livy.url");

        configuration = conf;

        return conf;
    }
}
