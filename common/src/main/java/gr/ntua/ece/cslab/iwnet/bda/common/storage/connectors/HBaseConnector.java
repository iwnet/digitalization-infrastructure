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

package gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors;

import com.google.protobuf.ServiceException;
import gr.ntua.ece.cslab.iwnet.bda.common.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HBaseConnector implements Connector {
    private final static Logger LOGGER = Logger.getLogger(HBaseConnector.class.getCanonicalName());

    private String namespace;
    private Connection connection;
    private org.apache.hadoop.conf.Configuration hbaseConfiguration;

    /**
     * Creates a new `HBaseConnector` instance, checks HBase availability and initializes a connection.
     *
     * TODO: Should use `usename`, `password` for connection.
     *
     * @param fs            The URL of the EventLog database.
     * @param username      The username to connect to the EventLog database.
     * @param password      The password to connect to the EventLog database.
     * @param configuration The Global BDA configuration.
     * @throws IOException
     * @throws ServiceException
     */
    public HBaseConnector(String fs, String username, String password, Configuration configuration)
        throws IOException, ServiceException {
        LOGGER.log(Level.INFO, "Initializing HBase Connector.");

        // Get the EventLog's Namespace.
        namespace = getHBaseNamespace(fs);

        // Initialize HBase Configuration.
        hbaseConfiguration = createHBaseConfiguration(fs, configuration);

        // Check HBase Availability.
        try {
            HBaseAdmin.checkHBaseAvailable(hbaseConfiguration);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "HBase Availability Check Failed.");
            throw e;
        }

        // Initialize HBase Connection.
        connection = null;
        try {
            connection = ConnectionFactory.createConnection(hbaseConfiguration);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Connection Failed! Check output console.");
            throw e;
        }

        LOGGER.log(Level.INFO, "HBase Connector initialized.");
    }

    /**
     * Creates a new `HBaseConfiguration` instance based on `fs` and `configuration`.
     *
     * The `fs` String contains the name of the EventLog database,
     * however this name is not used in the case of HBase since we create Namespaces.
     *
     * @param fs            The URL of the EventLog database.
     * @param configuration The Global BDA configuration.
     * @return              A `org.apache.hadoop.conf.Configuration` instance.
     */
    private static org.apache.hadoop.conf.Configuration createHBaseConfiguration(String fs, Configuration configuration) {
        org.apache.hadoop.conf.Configuration localHBaseConfiguration = HBaseConfiguration.create();

        localHBaseConfiguration.set("hbase.zookeeper.property.clientPort", getHBaseConnectionPort(fs));
        localHBaseConfiguration.set("hbase.zookeeper.quorum", configuration.storageBackend.getEventLogQuorum());
        localHBaseConfiguration.set("hbase.master", configuration.storageBackend.getEventLogMaster());
        localHBaseConfiguration.set("hbase.client.keyvalue.maxsize","0");

        return localHBaseConfiguration;
    }

    /**
     * Creates a new Namespace at the EventLog database.
     *
     * Based on the given `dbname` creates a new Namespace with the name `dbname + ":Events"`.
     *
     * @param fs            The URL of the EventLog database.
     * @param username      The username to connect to the EventLog database.
     * @param password      The password to connect to the EventLog database.
     * @param configuration The Global BDA configuration.
     * @param dbname        The name of the new Namespace to create.
     * @return              A String with the full path to the Namespace (including server URL).
     * @throws IOException
     * @throws ServiceException
     */
    public static String createNamespace(String fs, String username, String password, Configuration configuration,
                                         String dbname) throws IOException, ServiceException {
        // Initialize HBase Configuration.
        org.apache.hadoop.conf.Configuration localHbaseConfiguration = HBaseConnector.createHBaseConfiguration(
            fs, configuration);

        // Check HBase Availability.
        try {
            HBaseAdmin.checkHBaseAvailable(localHbaseConfiguration);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "HBase Availability Check Failed.");
            throw e;
        }

        // Initialize HBase Connection.
        Admin admin;
        try {
            admin = ConnectionFactory.createConnection(localHbaseConfiguration).getAdmin();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Admin Connection Failed! Check output console.");
            throw e;
        }

        LOGGER.log(Level.INFO, "HBase connection initialized.");

        // Create HBase namespace
        try {
            admin.createNamespace(NamespaceDescriptor.create(dbname).build());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Creation of namespace failed! Check output console.");
            throw e;
        }

        LOGGER.log(Level.INFO, "HBase namespace created.");

        try {
            TableName tableName = TableName.valueOf(dbname + ":Events");
            HTableDescriptor desc = new HTableDescriptor(tableName);
            desc.addFamily(new HColumnDescriptor("messages"));

            admin.createTable(desc);
            admin.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Creation of Events table in namespace failed! Check output console.");
            throw e;
        }

        LOGGER.log(Level.INFO, "HBase namespace created.");

        return fs + dbname;
    }

    public static void dropNamespace(String fs, String username, String password, Configuration configuration,
                                     String dbname) throws IOException, ServiceException {
        // Initialize HBase Configuration.
        org.apache.hadoop.conf.Configuration localHbaseConfiguration = HBaseConnector.createHBaseConfiguration(
                fs, configuration);

        // Check HBase Availability.
        try {
            HBaseAdmin.checkHBaseAvailable(localHbaseConfiguration);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "HBase Availability Check Failed.");
            throw e;
        }

        // Initialize HBase Admin Connection.
        Admin admin;
        try {
            admin = ConnectionFactory.createConnection(localHbaseConfiguration).getAdmin();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Admin Connection Failed! Check output console.");
            throw e;
        }

        LOGGER.log(Level.INFO, "HBase Admin connection initialized.");

        // Delete HBase table and namespace
        try {
            TableName table = TableName.valueOf(dbname + ":Events");
            admin.disableTable(table);
            admin.deleteTable(table);
            admin.deleteNamespace(dbname);
            admin.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Destroy of namespace failed! Check output console.");
            throw e;
        }

        LOGGER.log(Level.INFO, "HBase namespace deleted.");
    }

    public void close(){
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Extracts the port from a HBase Connection URL.
     */
    private static String getHBaseConnectionPort(String fs) {
        String[] tokens = fs.split(":");
        
        tokens = tokens[tokens.length - 1].split("/");

        return tokens[0];
    }

    /**
     * Extracts the host from a HBase Connection URL.
     */
    private static String getHBaseConnectionURL(String fs) {
        String[] tokens = fs.split("://")[1].split(":");

        return tokens[0];
    }

    /**
     * Extracts the namespace from a HBase Connection URL.
     */
    private static String getHBaseNamespace(String fs) {
        String[] tokens = fs.split("/");

        return tokens[tokens.length - 1];
    }

    public String getNamespace() {
        return namespace;
    }
}
