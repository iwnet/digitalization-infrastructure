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

import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnector;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.Connector;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.KPI;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.KPITable;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.Tuple;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.connectors.KPIConnectorFactory;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.connectors.KPIConnector;

import java.util.List;

public class KPIBackend {
    private KPIConnector kpiConnector;

    public KPIBackend(String slug) throws SystemConnectorException {
        this.kpiConnector = KPIConnectorFactory.getInstance().generateConnector(SystemConnector.getInstance().getKPIconnector(slug));
    }

    public KPIBackend(Connector conn) {

    }

    public void create(KPITable kpiTable) throws Exception {
        this.kpiConnector.create(kpiTable);
    }

    public void insert(KPI kpi) throws Exception {
        this.kpiConnector.put(kpi);

    }

    public List<Tuple> fetch(String kpi_name, String type, Integer value) throws Exception {
        if (type.equals("rows")) {
            return this.kpiConnector.getLast(kpi_name, value);
        }
        else
            throw new Exception("type not found: " + type);
    }

    public List<Tuple> select(String kpi_name, Tuple filters) throws Exception {
        return this.kpiConnector.get(kpi_name, filters);
    }

    public KPITable getSchema(String kpi_name) throws Exception {
        return this.kpiConnector.describe(kpi_name);
    }

}
