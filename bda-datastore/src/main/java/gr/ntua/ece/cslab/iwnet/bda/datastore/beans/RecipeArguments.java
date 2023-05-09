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

@XmlRootElement(name = "RecipeArguments")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class RecipeArguments implements Serializable {
    private List<String> dimension_tables;
    private List<String> message_types;
    private List<String> other_args;

    public RecipeArguments() {
        this.dimension_tables = new LinkedList<>();
        this.message_types = new LinkedList<>();
        this.other_args = new LinkedList<>();
    }

    public List<String> getDimension_tables() {
        return dimension_tables;
    }

    public void setDimension_tables(List<String> dimension_tables) {
        this.dimension_tables = dimension_tables;
    }

    public List<String> getMessage_types() {
        return message_types;
    }

    public void setMessage_types(List<String> message_types) {
        this.message_types = message_types;
    }

    public List<String> getOther_args() {
        return other_args;
    }

    public void setOther_args(List<String> other_args) {
        this.other_args = other_args;
    }

    @Override
    public String toString() {
        return "RecipeArguments{" +
                "dimension_tables=" + dimension_tables +
                ", message_types=" + message_types +
                ", other_args=" + other_args +
                '}';
    }
}
