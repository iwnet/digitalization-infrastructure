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

import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.*;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.PostgresqlConnector;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.*;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatastorePostgresqlConnector implements DatastoreConnector {
    Logger LOGGER = Logger.getLogger(DatastorePostgresqlConnector.class.getCanonicalName());

    PostgresqlConnector conn;

    // The constructor creates a connection to the database provided in the 'jdbcURL' parameter.
    // The database should be up and running.
    public DatastorePostgresqlConnector(PostgresqlConnector conn){
        this.conn=conn;
    }

    // Used to initialize or append a message in the EventLog
    public String put(Message row) throws Exception {
        /*try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet rs = dbm.getTables(null, null, "Events", null);
            if (rs.next()) {
                // Table exists
                List<KeyValue> columns = describe("").getSchema().getColumnTypes();
                String values = "";
                String insertTableSQL = "INSERT INTO Events (";
                for (KeyValue element : row.getEntries()) {
                    insertTableSQL += element.getKey() + ",";
                    values += "?,";
                }
                insertTableSQL = insertTableSQL.substring(0, insertTableSQL.length() - 1) + ") VALUES (" + values.substring(0, values.length() - 1) + ");";

                PreparedStatement prepst = connection.prepareStatement(insertTableSQL);
                int i = 1;
                for (KeyValue field : row.getEntries()){
                    for (KeyValue value : columns) {
                        if (value.getKey().equals(field.getKey())) {
                            if (value.getValue().contains("integer"))
                                prepst.setInt(i, Integer.valueOf(field.getValue()));
                            else if (value.getValue().contains("timestamp"))
                                prepst.setTimestamp(i, Timestamp.valueOf(field.getValue()));
                            else if (value.getValue().contains("bytea"))
                                prepst.setBytes(i, field.getValue().getBytes());
                            else if (value.getValue().contains("boolean"))
                                prepst.setBoolean(i, Boolean.parseBoolean(field.getValue()));
                            else
                                prepst.setString(i, field.getValue());
                        }
                    }
                    i++;
                }
                prepst.executeUpdate();
            }
            else {
                // Table does not exist
                Statement st = connection.createStatement();
                String q="CREATE TABLE Events (";
                for (KeyValue fields : row.getEntries()){
                    q += fields.getKey()+" "+fields.getValue()+",";
                }
                // add one more column named 'message' that will contain the blob
                q=q.substring(0, q.length() - 1)+");";
                LOGGER.log(Level.INFO, q);
                st.executeUpdate(q);
                st.executeUpdate("ALTER TABLE Events OWNER TO "+ this.user+";");
            }
            connection.commit();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            connection.rollback();
        }*/
        throw new java.lang.UnsupportedOperationException("Creating EventLog in Postgres is not supported.");
    }

    // Create dimension table and populate it
    public void put(MasterData masterData) throws Exception {
        try {
            Statement st = conn.getConnection().createStatement();
            for (DimensionTable table: masterData.getTables()) {
                List<KeyValue> columns = table.getSchema().getColumnTypes();
                String primaryKey = table.getSchema().getPrimaryKey();
                st.addBatch("DROP TABLE IF EXISTS "+table.getName()+";");

                String q="CREATE TABLE " + table.getName() + " (";
                for (KeyValue element : columns){
                    q+=element.getKey()+" "+element.getValue();
                    if (element.getKey().equals(primaryKey))
                        q+=" PRIMARY KEY";
                    q+=",";
                }
                q=q.substring(0, q.length() - 1)+");";
                LOGGER.log(Level.INFO, q);
                st.addBatch(q);
                st.addBatch("ALTER TABLE " + table.getName() + " OWNER TO "+ conn.getUsername()+";");
                st.executeBatch();

                // fill-in column values
                List<Tuple> data = table.getData();
                if (data.size()>0) {
                    String values = "";
                    String insertTableSQL = "INSERT INTO " + table.getName() + " (";
                    for (KeyValue element : columns) {
                        insertTableSQL += element.getKey() + ",";
                        if (element.getValue().equals("jsonb"))
                            values += "?::jsonb,";
                        else
                            values += "?,";
                    }
                    insertTableSQL = insertTableSQL.substring(0, insertTableSQL.length() - 1) + ") VALUES (" + values.substring(0, values.length() - 1) + ");";

                    PreparedStatement prepst = conn.getConnection().prepareStatement(insertTableSQL);
                    for (Tuple tuple : data) {
                        int i = 1;
                        for (KeyValue element : tuple.getTuple()) {
                            for (KeyValue field : columns) {
                                if (field.getKey().equals(element.getKey())) {
                                    if (field.getValue().contains("integer"))
                                        if (element.getValue().equalsIgnoreCase("null"))
                                            prepst.setNull(i,Types.INTEGER);
                                        else
                                            prepst.setInt(i, Integer.valueOf(element.getValue()));
                                    else if (field.getValue().contains("numeric"))
                                        if (element.getValue().equalsIgnoreCase("null"))
                                            prepst.setNull(i,Types.NUMERIC);
                                        else
                                            prepst.setFloat(i, Float.valueOf(element.getValue()));
                                    else if (field.getValue().contains("timestamp"))
                                        if (element.getValue().equalsIgnoreCase("null"))
                                            prepst.setNull(i,Types.TIMESTAMP);
                                        else
                                            prepst.setTimestamp(i, Timestamp.valueOf(element.getValue()));
                                    else if (field.getValue().contains("bytea"))
                                        if (element.getValue().equalsIgnoreCase("null"))
                                            prepst.setNull(i,Types.BINARY);
                                        else
                                            prepst.setBytes(i, element.getValue().getBytes());
                                    else if (field.getValue().contains("boolean"))
                                        if (element.getValue().equalsIgnoreCase("null"))
                                            prepst.setNull(i,Types.BOOLEAN);
                                        else
                                            prepst.setBoolean(i, Boolean.parseBoolean(element.getValue()));
                                    else if (field.getValue().contains("character varying"))
                                        if (element.getValue().equalsIgnoreCase("null"))
                                            prepst.setNull(i,Types.VARCHAR);
                                        else
                                            prepst.setString(i, element.getValue());
                                    else if (field.getValue().contains("jsonb"))
                                        prepst.setString(i, element.getValue());
                                    else
                                        if (element.getValue().equalsIgnoreCase("null"))
                                            prepst.setNull(i,Types.NULL);
                                        else
                                            prepst.setString(i, element.getValue());
                                }
                            }
                            i++;
                        }
                        prepst.addBatch();
                    }
                    prepst.executeBatch();
                }
                conn.getConnection().commit();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed creation");
            e.printStackTrace();
            conn.getConnection().rollback();
        }
    }

    // get last num rows from EventLog
    public List<Tuple> getLast(Integer num) throws Exception {
        /*List<Tuple> res = new LinkedList<>();
        try {
            Statement st = connection.createStatement();
            // Turn use of the cursor on.
            st.setFetchSize(1000);
            ResultSet rs = st.executeQuery("SELECT * FROM Events order by timestamp desc limit "+num+";");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                List<KeyValue> entries = new LinkedList<>();
                for (int i = 1; i <= columnsNumber; i++) {
                    String columnValue = rs.getString(i);
                    if (!columnValue.equalsIgnoreCase("null") && !columnValue.matches(""))
                        entries.add(new KeyValue(rsmd.getColumnName(i), columnValue));
                }
                res.add(new Tuple(entries));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            connection.rollback();
        }
        return res;*/
        throw new java.lang.UnsupportedOperationException("The EventLog is not set up in Postgres and can not be queried.");
    }


    public List<Tuple> getFrom(Integer args){
        throw new java.lang.UnsupportedOperationException("The EventLog is not set up in Postgres and can not be queried.");
    }

    // Get rows matching a specific column filter from a table
    public List<Tuple> get(String tablename, HashMap<String,String> filters) throws Exception {
        List<Tuple> res = new LinkedList<>();
        if (tablename.matches("")){
            /*DimensionTable table = this.describe(tablename);
            List<KeyValue> columns = table.getSchema().getColumnTypes();
            for (Map.Entry<String,String> filter: filters.entrySet()) {
                for (KeyValue field : columns) {
                    if (field.getKey().equals(filter.getKey())) {
                        if (field.getValue().contains("bytea"))
                            throw new Exception("Cannot filter the raw message in the eventLog.");
                    }
                }
            }*/
            throw new java.lang.UnsupportedOperationException("The EventLog is not set up in Postgres and can not be queried.");
        }
        try {
            Statement st = conn.getConnection().createStatement();
            // Turn use of the cursor on.
            st.setFetchSize(1000);
            String q = "SELECT * FROM "+tablename;
            if (!filters.isEmpty()) {
                q += " WHERE ";

                for (Map.Entry element : filters.entrySet()) {
                    q += "cast(" + element.getKey() + " as text) ='" + element.getValue() + "' AND ";
                }
                q = q.substring(0, q.length() - 4) + ";";
            }
            ResultSet rs = st.executeQuery(q);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            while (rs.next()) {
                List<KeyValue> entries = new LinkedList<>();
                for (int i = 1; i <= columnsNumber; i++) {
                    String columnValue = rs.getString(i);
                    entries.add(new KeyValue(rsmd.getColumnName(i), columnValue));
                }
                res.add(new Tuple(entries));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            conn.getConnection().rollback();
        }
        return res;
    }

    // get column names and types for table 'args'
    public DimensionTable describe(String args) throws Exception {
        if (args.matches(""))
            throw new java.lang.UnsupportedOperationException("The EventLog is not set up in Postgres and can not be queried.");
        List<String> columnNames = new LinkedList<>();
        List<KeyValue> columnTypes = new LinkedList<>();
        try {
            Statement st = conn.getConnection().createStatement();
            // Turn use of the cursor on.
            st.setFetchSize(1000);
            ResultSet rs = st.executeQuery("select column_name, data_type from INFORMATION_SCHEMA.COLUMNS where table_name = '"+args+"';");
            while (rs.next()) {
                columnNames.add(rs.getString(1));
                columnTypes.add(new KeyValue(rs.getString(1),rs.getString(2)));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            conn.getConnection().rollback();
        }
        return new DimensionTable(args,
                new DimensionTableSchema(columnNames, columnTypes, ""),
                new LinkedList<>());
    }

    // List dimension tables in database
    public List<String> list() {
        List<String> tables = new LinkedList<>();
        try {
            DatabaseMetaData dbm = conn.getConnection().getMetaData();
            ResultSet rs = dbm.getTables(null, null, "%", new String[] {"TABLE"});
            while (rs.next())
                if (!rs.getString("TABLE_NAME").equalsIgnoreCase("Events"))
                    tables.add(rs.getString("TABLE_NAME"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }
}
