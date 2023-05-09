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
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.LocalFSConnector;

import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.*;
import org.apache.commons.io.input.ReversedLinesFileReader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class DatastoreLocalFSConnector implements DatastoreConnector {

    LocalFSConnector conn;

    // The constructor creates the filesystem folder using the 'FS' parameter.
    // If this folder exists, it should be initially empty (before the bootstraping).
    public DatastoreLocalFSConnector(LocalFSConnector conn){
        this.conn=conn;
    }

    // Used to initialize or append a message in the EventLog which is a csv file
    public String put(Message row) throws Exception {
        File evlog = new File(conn.getFS() + "/EventLog.csv");
        FileWriter fw;
        BufferedWriter bw;
        if (!evlog.exists()) {
            // Initialize eventLog by writing column names in first line
            fw = new FileWriter(evlog);
            bw = new BufferedWriter(fw);
            for (KeyValue fields : row.getEntries()){
                String key = fields.getKey();
                bw.write( key + "\t");
            }
            bw.newLine();
        }
        else {
            // Append message in csv
            BufferedReader reader = new BufferedReader(new FileReader(conn.getFS() + "/EventLog.csv"));
            String[] fields = reader.readLine().split("\t");
            reader.close();
            fw = new FileWriter(evlog, true);
            bw = new BufferedWriter(fw);
            List<KeyValue> entries = row.getEntries();
            for (String field : fields) {
                String value = "null";
                for (KeyValue entry : entries) {
                    if (entry.getKey().equalsIgnoreCase(field)) {
                        value = entry.getValue();
                        break;
                    }
                }
                bw.write(value+"\t");
            }
            bw.newLine();
        }
        bw.close();
        fw.close();

        LineNumberReader lnr = new LineNumberReader(new FileReader(evlog));
        lnr.skip(Long.MAX_VALUE);
        Integer num = lnr.getLineNumber();
        lnr.close();
        return num.toString();
    }

    // Create table, populate it and store it in csv file
    public void put(MasterData masterData) throws Exception {
        for (DimensionTable table: masterData.getTables()) {
            String output = table.getName() + ".csv"; // save in csv
            FileWriter fw = new FileWriter(conn.getFS() + '/' + output);
            BufferedWriter bw = new BufferedWriter(fw);

            List<Tuple> data = table.getData();
            // write column names
            List<KeyValue> fields = data.get(0).getTuple();
            for (KeyValue element : fields)
                bw.write(element.getKey() + "\t");
            bw.newLine();
            // fill-in column values
            for (Tuple tuple : data) {
                for (KeyValue element : tuple.getTuple())
                    bw.write(element.getValue() + "\t");
                bw.newLine();
            }
            bw.close();
            fw.close();
        }
    }

    // get last num rows from EventLog
    public List<Tuple> getLast(Integer num) throws IOException {
        List<Tuple> res = new LinkedList<>();
        File file = new File(conn.getFS() + "/EventLog.csv");
        List<String> fields = describe("").getSchema().getColumnNames();
        LineNumberReader lnr;

        // If num is negative get total number of rows
        if (num < 0) {
            lnr = new LineNumberReader(new FileReader(file));
            lnr.skip(Long.MAX_VALUE);
            num = lnr.getLineNumber()-1;
            lnr.close();
        }

        int counter = 0;
        String line;
        ReversedLinesFileReader reader = new ReversedLinesFileReader(file);
        // Read from end of file with a counter
        while((line = reader.readLine()) != null && counter < num){
            List<KeyValue> entries = new LinkedList<>();
            String[] values = line.split("\t");
            for (int i=0; i < fields.size(); i++) {
                String columnValue = values[i];
                if (!columnValue.equalsIgnoreCase("null") && !columnValue.matches(""))
                    entries.add(new KeyValue(fields.get(i),columnValue));
            }
            res.add(new Tuple(entries));
            counter++;
        }
        reader.close();
        return res;
    }

    // Get rows for last num days from EventLog
    public List<Tuple> getFrom(Integer num){
        throw new java.lang.UnsupportedOperationException();
    }

    // Get rows matching a specific column filter from a table
    public List<Tuple> get(String table, HashMap<String,String> filters) throws Exception {
        List<Tuple> res = new LinkedList<>();
        List<String> fields = describe(table).getSchema().getColumnNames();
        HashMap<Integer,String> positions = new HashMap<>();
        String line;

        for (Map.Entry<String,String> flt: filters.entrySet()) {
            Integer pos = fields.indexOf(flt.getKey());
            if ((pos == -1) && !(table.matches("") && flt.getKey().equalsIgnoreCase("key")))
                throw new Exception("Column not found in the table.");
            else if ((pos == -1) && (table.matches("") && flt.getKey().equalsIgnoreCase("key"))){
                try (Stream<String> lines = Files.lines(Paths.get(conn.getFS() + "/EventLog.csv"))) {
                    line = lines.skip(Long.parseLong(flt.getValue())-1).findFirst().get();
                }
                String[] values = line.split("\t");
                List<KeyValue> entries = new LinkedList<>();
                for (int i = 0; i < fields.size(); i++)
                    entries.add(new KeyValue(fields.get(i), values[i]));
                res.add(new Tuple(entries));
                return res;
            }
            else
                positions.put(pos,flt.getValue());
        }

        int counter = 0;
        if (table.matches(""))
            table = "EventLog";
        ReversedLinesFileReader reader = new ReversedLinesFileReader(new File(conn.getFS() + "/" + table + ".csv"));
        // Read the table line by line and filter the specified column. In the eventLog only the last 1000 rows are searched.
        while((line = reader.readLine()) != null && (!table.equals("EventLog") || counter < 1000)) {
            String[] values = line.split("\t");
            boolean selected = true;
            for (Map.Entry<Integer, String> pos : positions.entrySet())
                if (!values[pos.getKey()].equals(pos.getValue()))
                    selected = false;
            if (selected) {
                List<KeyValue> entries = new LinkedList<>();
                for (int i = 0; i < fields.size(); i++)
                    entries.add(new KeyValue(fields.get(i), values[i]));
                res.add(new Tuple(entries));
            }
            counter++;
        }
        reader.close();
        return res;
    }

    // get column names for table args
    public DimensionTable describe(String args) throws IOException {
        String table;
        if (args.matches(""))
            table = conn.getFS() + "/EventLog.csv";
        else
            table = conn.getFS() + "/" + args + ".csv";
        BufferedReader reader = new BufferedReader(new FileReader(table));
        String[] fields = reader.readLine().split("\t");
        reader.close();
        return new DimensionTable(args,
                new DimensionTableSchema(Arrays.asList(fields), new LinkedList<>(), ""),
                new LinkedList<>());
    }

    // List dimension tables in FS
    public List<String> list() {
        File folder = new File(conn.getFS());
        File[] dimensiontables = folder.listFiles((dir, name) -> (!name.contains("EventLog")));
        List<String> tables = new LinkedList<>();
        for (File file: dimensiontables)
            tables.add(file.getName().split("\\.")[0]);
        return tables;
    }
}
