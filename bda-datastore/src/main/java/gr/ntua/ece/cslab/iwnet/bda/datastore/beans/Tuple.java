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

@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Tuple implements Serializable {
    private List<KeyValue> tuple;

    public Tuple(){ this.tuple = new LinkedList<>();}

    public Tuple(List<KeyValue> tuple) {
        this.tuple = tuple;
    }

    public List<KeyValue> getTuple() {
        return this.tuple;
    }

    public void setTuple(List<KeyValue> tuple) {
        this.tuple = tuple;
    }

    public String toString() {
        String row = this.tuple.stream().
                map(a -> "["+a.getKey() + "," + a.getValue()+"]").
                reduce((a, b) -> a + "," + b).
                get();
        return row;
    }
}
