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

import java.util.LinkedList;
import java.util.List;

public class KPI {
    String kpi_name;
    String timestamp;
    List<KeyValue> entries;

    /*
        Empty Constructor
     */
    public KPI() {
        this.entries = new LinkedList<>();
    }

    public KPI(String kpi_name, String timestamp, List<KeyValue> entries) {
        this.kpi_name = kpi_name;
        this.timestamp = timestamp;
        this.entries = entries;
    }

    public String getKpi_name() {
        return kpi_name;
    }

    public void setKpi_name(String kpi_name) {
        this.kpi_name = kpi_name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public List<KeyValue> getEntries() {
        return entries;
    }

    public void setEntries(List<KeyValue> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "KPI{" +
                "kpi_name='" + kpi_name + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", entries=[" + entries +
                "]";
    }
}
