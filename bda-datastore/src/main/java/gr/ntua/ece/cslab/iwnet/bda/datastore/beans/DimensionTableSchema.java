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

package gr.ntua.ece.cslab.iwnet.bda.datastore.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "DimensionTableSchema")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class DimensionTableSchema {
    private List<String> columnNames;
    private List<KeyValue> columnTypes;
    private String primaryKey;

    public DimensionTableSchema(){ this.columnNames = new LinkedList<>();};

    public DimensionTableSchema(List<String> columnNames, List<KeyValue> columnTypes, String primaryKey) {
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
        this.primaryKey = primaryKey;
    }

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

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
}
