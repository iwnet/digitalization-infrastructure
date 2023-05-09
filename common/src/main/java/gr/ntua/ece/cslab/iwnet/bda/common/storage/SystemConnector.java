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

package gr.ntua.ece.cslab.iwnet.bda.common.storage;

import gr.ntua.ece.cslab.iwnet.bda.common.Configuration;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.DbInfo;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.Connector;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.ConnectorFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.UnsupportedOperationException;

public class SystemConnector {
    private final static Logger LOGGER = Logger.getLogger(SystemConnector.class.getCanonicalName());
    private static Configuration configuration;
    private static SystemConnector systemConnector;

    private Connector bdaConnector;
    private Connector hdfsConnector;
    private HashMap<String, Connector> elConnectors;
    private HashMap<String, Connector> dtConnectors;
    private HashMap<String, Connector> kpiConnectors;

    /** The constructor creates new connections for the EventLog FS, the Dimension
     *  tables FS and the KPI db per LL as well as the BDA db. **/
    private SystemConnector() throws SystemConnectorException {
        this.elConnectors = new HashMap<String, Connector>();
        this.dtConnectors = new HashMap<String, Connector>();
        this.kpiConnectors = new HashMap<String, Connector>();

        LOGGER.log(Level.INFO, "Initializing BDA db connector...");
        bdaConnector = ConnectorFactory.getInstance().generateConnector(
                configuration.storageBackend.getBdaDatabaseURL(),
                configuration.storageBackend.getDbUsername(),
                configuration.storageBackend.getDbPassword(),
                configuration
        );

        hdfsConnector = ConnectorFactory.getInstance().generateConnector(
            configuration.storageBackend.getHDFSMasterURL(),
            configuration.storageBackend.getHDFSUsername(),
            configuration.storageBackend.getHDFSPassword(),
            configuration
        );
    }

    public static SystemConnector getInstance() throws SystemConnectorException {
        if (systemConnector == null){
            systemConnector = new SystemConnector();
            systemConnector.initDBconnections();
        }
        return systemConnector;
    }

    public static void init(String args) throws SystemConnectorException {
        // parse configuration
        configuration = Configuration.parseConfiguration(args);
        if(configuration==null) {
            System.exit(1);
        }
        if (systemConnector == null) {
            systemConnector = new SystemConnector();
            systemConnector.initDBconnections();
        }
    }

    private void initDBconnections() throws SystemConnectorException {
        List<DbInfo> dbInfos = new LinkedList<>();
        try {
            dbInfos = DbInfo.getDbInfo();
            LOGGER.log(Level.INFO, "Initializing db connectors...");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
        for (DbInfo info: dbInfos){
            elConnectors.put(info.getSlug(), ConnectorFactory.getInstance().generateConnector(
                configuration.storageBackend.getEventLogURL() + info.getElDbname(),
                configuration.storageBackend.getDbUsername(),
                configuration.storageBackend.getDbPassword(),
                configuration
            ));

            dtConnectors.put(info.getSlug(), ConnectorFactory.getInstance().generateConnector(
                configuration.storageBackend.getDimensionTablesURL() + info.getDtDbname(),
                configuration.storageBackend.getDbUsername(),
                configuration.storageBackend.getDbPassword(),
                configuration
            ));
            /*PostgresqlPooledDataSource.init(
                    configuration.storageBackend.getBdaDatabaseURL(),
                    configuration.storageBackend.getDimensionTablesURL(),
                    configuration.storageBackend.getDbUsername(),
                    configuration.storageBackend.getDbPassword()
            );*/

            kpiConnectors.put(info.getSlug(), ConnectorFactory.getInstance().generateConnector(
                configuration.kpiBackend.getDbUrl() + info.getKpiDbname(),
                configuration.kpiBackend.getDbUsername(),
                configuration.kpiBackend.getDbPassword(),
                configuration
            ));
        }
    }

    public void createDatabase(DbInfo info)
        throws SystemConnectorException, UnsupportedOperationException {

        String slug = info.getSlug();
        Vector<String> schemas = new Vector<String>(1);
        schemas.add("metadata");

        String databaseUrl = ConnectorFactory.createNewDatabaseWithSchemas(
            configuration.storageBackend.getDimensionTablesURL(),
            configuration.storageBackend.getDbPrivilegedUsername(),
            configuration.storageBackend.getDbPrivilegedPassword(),
            configuration,
            configuration.storageBackend.getDbUsername(),
            info.getDtDbname(),
            schemas
        );

        Connector dtConnector = ConnectorFactory.getInstance().generateConnector(
                databaseUrl,
                configuration.storageBackend.getDbUsername(),
                configuration.storageBackend.getDbPassword(),
                configuration
        );

        dtConnectors.put(slug, dtConnector);

        databaseUrl = ConnectorFactory.createNewDatabaseWithSchemas(
                configuration.storageBackend.getEventLogURL(),
                configuration.storageBackend.getDbUsername(),
                configuration.storageBackend.getDbPassword(),
                configuration,
                configuration.storageBackend.getDbUsername(),
                info.getElDbname(),
                null
        );

        Connector elConnector = ConnectorFactory.getInstance().generateConnector(
                databaseUrl,
                configuration.storageBackend.getDbUsername(),
                configuration.storageBackend.getDbPassword(),
                configuration
        );

        elConnectors.put(slug, elConnector);

        databaseUrl = ConnectorFactory.createNewDatabaseWithSchemas(
                configuration.kpiBackend.getDbUrl(),
                configuration.storageBackend.getDbPrivilegedUsername(),
                configuration.storageBackend.getDbPrivilegedPassword(),
                configuration,
                configuration.kpiBackend.getDbUsername(),
                info.getKpiDbname(),
                null
        );

        Connector kpiConnector = ConnectorFactory.getInstance().generateConnector(
                databaseUrl,
                configuration.kpiBackend.getDbUsername(),
                configuration.kpiBackend.getDbPassword(),
                configuration
        );

        kpiConnectors.put(slug, kpiConnector);
    }

    public void destroyDatabase(DbInfo info)
            throws UnsupportedOperationException, SystemConnectorException {

        String slug = info.getSlug();
        if (dtConnectors.containsKey(slug))
            getDTconnector(slug).close();
        if (elConnectors.containsKey(slug))
            getELconnector(slug).close();
        if (kpiConnectors.containsKey(slug))
            getKPIconnector(slug).close();

        ConnectorFactory.dropDatabase(
                configuration.storageBackend.getDimensionTablesURL(),
                configuration.storageBackend.getDbPrivilegedUsername(),
                configuration.storageBackend.getDbPrivilegedPassword(),
                configuration,
                configuration.storageBackend.getDbUsername(),
                info.getDtDbname()
        );
        dtConnectors.remove(slug);

        ConnectorFactory.dropDatabase(
                configuration.storageBackend.getEventLogURL(),
                configuration.storageBackend.getDbUsername(),
                configuration.storageBackend.getDbPassword(),
                configuration,
                configuration.storageBackend.getDbUsername(),
                info.getElDbname()
        );
        elConnectors.remove(slug);

        ConnectorFactory.dropDatabase(
                configuration.kpiBackend.getDbUrl(),
                configuration.storageBackend.getDbPrivilegedUsername(),
                configuration.storageBackend.getDbPrivilegedPassword(),
                configuration,
                configuration.kpiBackend.getDbUsername(),
                info.getKpiDbname()
        );
        kpiConnectors.remove(slug);
    }

    public void close(){
        bdaConnector.close();

        for (Map.Entry<String, Connector> conn: elConnectors.entrySet()){
            conn.getValue().close();
        }
        for (Map.Entry<String, Connector> conn: dtConnectors.entrySet()){
            conn.getValue().close();
        }
        for (Map.Entry<String, Connector> conn: kpiConnectors.entrySet()){
            conn.getValue().close();
        }
    }

    public Connector getBDAconnector() {
        return bdaConnector;
    }

    public Connector getHDFSConnector() {
        return hdfsConnector;
    }

    public Connector getELconnector(String slug) {
        return elConnectors.get(slug);
    }

    public Connector getDTconnector(String slug) {
        return dtConnectors.get(slug);
    }

    public Connector getKPIconnector(String slug) {
        return kpiConnectors.get(slug);
    }
}
