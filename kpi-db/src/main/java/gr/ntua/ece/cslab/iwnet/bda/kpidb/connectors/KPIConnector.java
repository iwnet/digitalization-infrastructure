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

package gr.ntua.ece.cslab.iwnet.bda.kpidb.connectors;

import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.KPI;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.KPITable;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.Tuple;

import java.util.List;

public interface KPIConnector {
    /*
        Method create will be used upon KPI initilization
        in order to create the corresponding table in the KPIDB
     */
    void create(KPITable kpi_table) throws Exception;

    /*
        Method put will be used upo KPI computation
        in order to insert the computed value in the KPIDB table
     */
    void put(KPI kpi) throws Exception;


    /*
        Used to select computed KPIs upon column filtering
     */
    List<Tuple> get(String kpi_name, Tuple filters) throws Exception;

    /*
        Used to get the n most frequent computations of a KPI
     */
    List<Tuple> getLast(String kpi_name, Integer n) throws Exception;

    /*
        Get the schema of a KPI table
     */
    KPITable describe(String kpi_name) throws Exception;

    /*
        Get all the current KPIs
     */
    List<String> list();


}
