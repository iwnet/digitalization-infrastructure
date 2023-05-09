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

package gr.ntua.ece.cslab.iwnet.bda.kpidb;

import static java.lang.Thread.sleep;

public class KPIBackendTest {

    /*public KPIBackend kpiBackend;
    @Before
    public void setUp() {
        String fs_string = "jdbc:postgresql://bda-postgres:5432/test_db";
        String uname = "iwnet";
        String passwd = "123456";
        kpiBackend = new KPIBackend(fs_string, uname, passwd);
        System.out.println("Connection on kpidb successfully established");
    }

    @After
    public void tearDown() {
        kpiBackend.stop();
        System.out.println("Connection on kpidb successfully closed");
    }

    public void create() {
        KPISchema kpiSchema = new KPISchema(new ArrayList<>(), new ArrayList<>());
        kpiSchema.getColumnNames().add("supplier_id");
        kpiSchema.getColumnNames().add("warehouse_id");
        kpiSchema.getColumnNames().add("blob");
        kpiSchema.getColumnTypes().add(new KeyValue("supplier_id","integer"));
        kpiSchema.getColumnTypes().add(new KeyValue("warehouse_id","integer"));
        kpiSchema.getColumnTypes().add(new KeyValue("blob","jsonb"));
        try {
            this.kpiBackend.create(new KPITable("test_kpi", kpiSchema));
            System.out.println("Table creation completed successfully");
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println("Exception on table creation");
        }
    }

    public void insert() {
        for (int i = 0; i < 10; i++) {
            int supplier;
            int warehouse;
            if (i < 5) {
                supplier = 1;
            }
            else {
                supplier = 2;
            }
            warehouse = i;
            List<KeyValue> entries = new ArrayList<>();
            entries.add(new KeyValue("supplier_id", String.valueOf(supplier)));
            entries.add(new KeyValue("warehouse_id", String.valueOf(warehouse)));
            entries.add(new KeyValue("blob",
                    "[{\"id\":1, \"id2\" : 2, \"rest\" : " +
                            "[{\"rest1\" : 1, \"rest2\" : 2}, {\"rest1\" : 3, \"rest2\" : 4}]}," +
                            "{\"id\":1, \"id2\" : 2, \"rest\" : " +
                            "[{\"rest1\" : 1, \"rest2\" : 2}, {\"rest1\" : 3, \"rest2\" : 4}]}]"));
            KPI kpi = new KPI("test_kpi", (new Timestamp(System.currentTimeMillis())).toString(), entries);
            try {
                this.kpiBackend.insert(kpi);
                System.out.println("Insertion of a kpi completed successfully");
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void fetch() {
        try {
            List<Tuple> tuples = this.kpiBackend.fetch("test_kpi", "rows", 3);
            System.out.println("Successfully fetched last "+tuples.size()+" rows");
            for (Tuple t : tuples) {
                System.out.println(t.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void select() {
        List<Tuple> tuples = null;
        Tuple filters = null;
        System.out.println("Test select *");
        filters = new Tuple();
        try {
            tuples = this.kpiBackend.select("test_kpi", filters);
            System.out.println("Successfully selected all rows");
            for (Tuple t : tuples) {
                System.out.println(t.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("Test select on 1 arg");
        filters.getTuple().add(new KeyValue("supplier_id","1"));
        try {
            tuples = this.kpiBackend.select("test_kpi", filters);
            System.out.println("Successfully selected " + tuples.size() + " rows");
            for (Tuple t : tuples) {
                System.out.println(t.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("Test select on 2 arg");
        filters.getTuple().add(new KeyValue("warehouse_id","2"));
        try {
            tuples = this.kpiBackend.select("test_kpi", filters);
            System.out.println("Successfully selected " + tuples.size() + " rows");
            for (Tuple t : tuples) {
                System.out.println(t.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public KPITable getSchema() {
        KPITable table = null;
        try {
            table = this.kpiBackend.getSchema("test_kpi");
            System.out.println("Fetch table schema completed successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return table;
    }

    @Test
    public void test() {
        create();

        System.out.println(getSchema().toString());

        insert();

        fetch();

        select();
    }*/
}