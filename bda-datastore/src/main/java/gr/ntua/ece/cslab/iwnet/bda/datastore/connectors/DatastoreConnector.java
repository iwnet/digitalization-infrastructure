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

package gr.ntua.ece.cslab.iwnet.bda.datastore.connectors;

import java.util.HashMap;
import java.util.List;

import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.*;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.DimensionTable;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.MasterData;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Message;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Tuple;

/** Methods that a connector should implement for accessing the filesystem. **/
public interface DatastoreConnector {
    String put(Message args) throws Exception;
    void put(MasterData args) throws Exception;
    List<Tuple> getLast(Integer args) throws Exception;
    List<Tuple> getFrom(Integer args) throws Exception;
    List<Tuple> get(String args, HashMap<String,String> args2) throws Exception;
    DimensionTable describe(String args) throws Exception;
    List<String> list();
}
