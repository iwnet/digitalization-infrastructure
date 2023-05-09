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

package gr.ntua.ece.cslab.iwnet.bda.datastore;

import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.*;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.DbInfo;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnector;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.*;
import gr.ntua.ece.cslab.iwnet.bda.datastore.connectors.DatastoreConnector;
import gr.ntua.ece.cslab.iwnet.bda.datastore.connectors.ConnectorFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class StorageBackend {

    private DatastoreConnector ELconnector;
    private DatastoreConnector DTconnector;

    /** The StorageBackend constructor creates two new connections, one for the EventLog FS and one for the Dimension
     *  tables FS, using the FS parameters that are provided as input Strings. **/
    public StorageBackend(String slug) throws SystemConnectorException {
        this.ELconnector = ConnectorFactory.getInstance().generateConnector(SystemConnector.getInstance().getELconnector(slug));
        this.DTconnector = ConnectorFactory.getInstance().generateConnector(SystemConnector.getInstance().getDTconnector(slug));
    }

    public static void createNewDB(DbInfo info)
            throws SystemConnectorException, UnsupportedOperationException, SQLException {
        // 1. Create databases for the Dimension Tables (with data/metadata schemas), the EventLog and the KPIdb.
        SystemConnector.getInstance().createDatabase(info);

        // 2. Create metadata tables.
        MessageType.createTable(info.getSlug());
        Recipe.createTable(info.getSlug());
        Job.createTable(info.getSlug());
    }

    public static void destroyDBs(DbInfo info)
            throws UnsupportedOperationException, SystemConnectorException {
        // Destroy the databases of the Dimension Tables, the EventLog and the KPIdb.
        SystemConnector.getInstance().destroyDatabase(info);
    }

    /** Initialize the eventLog and dimension tables in the underlying FS Using the masterData.
     *  MasterData include in json format for all the dimension tables the name, schema, data and the primary key of
     *  each table. Using the primary keys of the dimension tables the eventLog is created with these foreign keys
     *  and an extra column named 'message' is created in the eventLog that contains the actual message (that will
     *  be in json format). **/
    public void init(MasterData masterData) throws Exception {
        DTconnector.put(masterData);
        /*List<KeyValue> columns = new LinkedList<>();
        for (DimensionTable table: masterData.getTables()) {
            String key = table.getSchema().getPrimaryKey();
            String type = "";
            for (KeyValue column : table.getSchema().getColumnTypes())
                if (column.getKey().matches(key))
                    type = column.getValue();
            KeyValue field = new KeyValue(table.getName()+"_"+key, type);
            columns.add(field);
        }
        Message emptyMsg = new Message(new LinkedList<>(), columns);
        ELconnector.put(emptyMsg);*/
    }

    /** Insert a new message in the EventLog.
     *  This method takes as input a Message and saves each key that matches with an EventLog column name in the
     *  relevant column of the eventLog table, while all the non-matching keys are saved as a blob in json format
     *  in the 'message' column of the eventLog table. **/
    public String insert(Message message) throws Exception {
        // Convert message to appropriate format taking into account the schema
        /*JSONObject json = new JSONObject(); // to store blob
        List<String> fields = ELconnector.describe("").getSchema().getColumnNames();
        HashMap<String, String> msg = new HashMap<>();
        for (KeyValue element : message.getEntries()) {
            if (!fields.contains(element.getKey()))
                json.put(element.getKey(), element.getValue());
            else
                msg.put(element.getKey(), element.getValue());
        }
        for (String column : fields)
            if (!msg.containsKey(column))
                msg.put(column, "null");
        msg.put("payload", json.toJSONString());
        msg.put("event_timestamp", String.valueOf(LocalDateTime.now()));

        if (msg.containsKey("payload") && msg.containsKey("message_type") && msg.containsKey("slug") && msg.size() == 4)
            throw new Exception("Message does not contain any foreign keys.");
        else if (json.isEmpty() || !msg.containsKey("message_type") || msg.size() < 4)
            throw new Exception("Message contains strange event format. Append aborted.");*/
        return ELconnector.put(message);
    }

    /** Get rows from EventLog. Fetches either the last n messages or the messages received the last n days.
     *  This method requires as input a string that denotes 'days' or 'rows' and an integer that denotes the
     *  number n. **/
    public List<Tuple> fetch(String type, Integer value) throws Exception {
        if (type.equals("rows"))
            return ELconnector.getLast(value);
        else if (type.equals("days"))
            return ELconnector.getFrom(value);
        else
            throw new Exception("type not found: " + type);
    }

    /** Select rows filtered in a specific column with a specific value from a dimension table.
     *  This method requires as input a string which is the dimension table name, the column name and the column value
     *  as strings. **/
    public List<Tuple> select(String table, HashMap<String,String> filters) throws Exception {
        return DTconnector.get(table, filters);
    }

    /** Select rows filtered in a specific column with a specific value from the eventLog table.
     *  This method requires as input the column name and the column value as strings. The eventLog can be filtered in
     *  a column that is a foreign key to a dimension table, not in the actual message and the last 1000 messages are
     *  searched.**/
    public List<Tuple> select(HashMap<String,String> filters) throws Exception {
        return ELconnector.get("", filters);
    }

    /** Get table schema.
     *  This method takes as input a string which is the dimension table name or an empty string if it refers to
     *  the eventLog table. **/
    public DimensionTable getSchema(String table) throws Exception {
        if (table.matches(""))
            return ELconnector.describe(table);
        else
            return DTconnector.describe(table);
    }

    /** List dimension tables. **/
    public List<String> listTables() {
            return DTconnector.list();
    }

}
