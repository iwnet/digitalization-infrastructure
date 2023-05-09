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

import org.json.JSONObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@XmlRootElement(name = "DimensionTableSchema")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class KPISchema {
    private List<String> columnNames;
    private List<KeyValue> columnTypes;

    /* Empty constructor */
    public KPISchema() {
    }

    /* Constructor on fields*/
    public KPISchema(List<String> columnNames, List<KeyValue> columnTypes) {
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
    }

    public KPISchema(JSONObject msgFormat) {
        this.columnNames = new ArrayList<>();
        this.columnTypes = new ArrayList<>();

        Iterator<String> columns = msgFormat.keys();
        while (columns.hasNext()) {
            String name = columns.next();
            if (!name.matches("message_type") && !name.matches("slug") && !name.matches("payload")) {
                this.columnNames.add(name);
                // TODO: support in RecipeDataLoader reading data from hbase not only as strings
                this.columnTypes.add(new KeyValue(name, "varchar(256)"));//msgFormat.getString(name)));
            }
        }
        this.columnNames.add("result");
        this.columnTypes.add(new KeyValue("result", "jsonb"));
    }

    /* Getters and Setters */
    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<KeyValue> getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(List<KeyValue> columnTypes) {
        this.columnTypes = columnTypes;
    }

    @Override
    public String toString() {
        return "KPISchema{" +
                this.columnTypes.stream().map(a -> "["+a.getKey() + "," + a.getValue()+"]").reduce((a, b) -> a + "," + b).get() +
                '}';
    }
}
