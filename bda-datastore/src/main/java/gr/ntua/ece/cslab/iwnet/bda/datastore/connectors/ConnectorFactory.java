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

package gr.ntua.ece.cslab.iwnet.bda.datastore.connectors;

import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.Connector;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.HBaseConnector;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.LocalFSConnector;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.PostgresqlConnector;

public class ConnectorFactory {
    private static ConnectorFactory connFactory;

    private ConnectorFactory() {}

    public static ConnectorFactory getInstance(){
        if (connFactory == null)
            connFactory = new ConnectorFactory();
        return connFactory;
    }

    /** Depending on the FS string format initialize a connector from a different class.
     *  Connectors are implemented for four different filesystems: local, HBase, HDFS, PostgreSQL. **/
    public DatastoreConnector generateConnector(Connector conn){
        DatastoreConnector connector = null;
        if ( conn instanceof HBaseConnector ){
            connector = new DatastoreHBaseConnector( (HBaseConnector) conn);
        }
        else if (conn instanceof PostgresqlConnector) {
            connector = new DatastorePostgresqlConnector( (PostgresqlConnector) conn);
        }
        else if (conn instanceof LocalFSConnector) {
            connector = new DatastoreLocalFSConnector( (LocalFSConnector) conn);
        }
        return connector;
    }
}
