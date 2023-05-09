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

package gr.ntua.ece.cslab.iwnet.bda.kpidb.beans;

public class KPITable {
    private String kpi_name;
    private KPISchema kpi_schema;

    public KPITable(String kpi_name, KPISchema kpi_schema) {
        this.kpi_name = kpi_name;
        this.kpi_schema = kpi_schema;
    }

    public String getKpi_name() {
        return kpi_name;
    }

    public void setKpi_name(String kpi_name) {
        this.kpi_name = kpi_name;
    }

    public KPISchema getKpi_schema() {
        return kpi_schema;
    }

    public void setKpi_schema(KPISchema kpi_schema) {
        this.kpi_schema = kpi_schema;
    }

    @Override
    public String toString() {
        return "KPITable{" +
                "kpi_name='" + kpi_name + '\'' +
                ", kpi_schema=" + kpi_schema.toString() +
                '}';
    }
}
