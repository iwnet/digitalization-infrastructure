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
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * MasterData represents the master data to be inserted to the datastore.
 */
@XmlRootElement(name = "MasterData")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class MasterData implements Serializable{
    private List<DimensionTable> tables;

    public MasterData() { this.tables = new LinkedList<>();};

    public MasterData(List<DimensionTable> tables) {
        this.tables = tables;
    }

    public List<DimensionTable> getTables() {
        return tables;
    }

    public void setTables(List<DimensionTable> tables) {
        this.tables = tables;
    }
}
