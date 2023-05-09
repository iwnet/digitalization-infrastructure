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

package gr.ntua.ece.cslab.iwnet.bda.controller.resources;

import com.google.common.base.Splitter;

import gr.ntua.ece.cslab.iwnet.bda.kpidb.KPIBackend;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.KPI;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.KPITable;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.KeyValue;
import gr.ntua.ece.cslab.iwnet.bda.kpidb.beans.Tuple;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * KPIResource holds the method for the analyticsml module.
 */
@RestController
@CrossOrigin(origins = {"https://iwnet.qubiteq.gr","http://localhost:3000"}, allowCredentials = "true")
@RequestMapping("kpi")
public class KPIResource {
    @GetMapping()
    public List<KPI> getKPIList() {
        // TODO: implement the method
        return new LinkedList<>();
    }

    @PostMapping("{id}/run")
    public ResponseEntity<?> runKPI(@PathVariable("id") String id) {
        // TODO: implement the method
        return ResponseEntity.ok("");
    }

    @GetMapping(value = "{slug}/{kpiname}/fetch", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> getLastKPIs(
            @PathVariable("kpiname") String kpiname,
            @PathVariable("slug") String slug,
            @RequestParam("n") Integer n,
            @RequestHeader("Accept") String accepted
    ) {

        try {
            KPIBackend kpiBackend = new KPIBackend(slug);
            List<Tuple> results = kpiBackend.fetch(kpiname, "rows", n);
            KPITable table = kpiBackend.getSchema(kpiname);
            JSONArray returnResults = new JSONArray();
            for (Tuple tuple : results) {
                JSONObject row = new JSONObject();
                for (KeyValue cell : tuple.getTuple()) {
                    for (KeyValue type : table.getKpi_schema().getColumnTypes()) {
                        if (cell.getKey().equals(type.getKey())) {
                            if (type.getValue().contains("integer")) {
                                if (cell.getValue() == "null") {
                                    cell.setValue("-1");
                                }
                                row.put(cell.getKey(), Integer.valueOf(cell.getValue()));
                            }
                            else if (type.getValue().contains("bigint")) {
                                if (cell.getValue() == "null") {
                                    cell.setValue("-1");
                                }
                                row.put(cell.getKey(), Long.valueOf(cell.getValue()));
                            }
                            else if (type.getValue().contains("json")) {
                                if (cell.getValue() == "null") {
                                    cell.setValue("{}");
                                }
                                if (cell.getValue().startsWith("["))
                                    row.put(cell.getKey(), new JSONArray(cell.getValue()));
                                else
                                    row.put(cell.getKey(), new JSONObject(cell.getValue()));
                            }
                            else {
                                if (cell.getValue() == "null") {
                                    cell.setValue("");
                                }
                                row.put(cell.getKey(), cell.getValue());
                            }
                        }
                    }
                }
                returnResults.put(row);
            }

            if(accepted != null) {
                MediaType mediaType = MediaType.valueOf(accepted);
                if (mediaType.equals(MediaType.APPLICATION_XML))
                    return ResponseEntity.ok(XML.toString(returnResults));
                else if (mediaType.equals(MediaType.valueOf(String.valueOf(MediaType.APPLICATION_JSON))))
                    return ResponseEntity.ok(returnResults.toString());
            }
            // service logic
            return ResponseEntity.ok(XML.toString(returnResults));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "{slug}/{kpiname}/select", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> selectKPIs(
            @PathVariable("kpiname") String kpiname,
            @PathVariable("slug") String slug,
            @RequestParam("filters") String filters,
            @RequestHeader("Accept") String accepted
    ) {
        List<KeyValue> args = new LinkedList<>();
        if(filters != null && !filters.isEmpty()) {
            System.out.println("Select parameters given");
            Map<String, String> myfilters = Splitter.on(';').withKeyValueSeparator(":").split(filters);
            for (Map.Entry entry : myfilters.entrySet()) {
                //System.out.println(entry.getKey() + " , " + entry.getValue());
                args.add(new KeyValue(entry.getKey().toString(), entry.getValue().toString()));
            }
        }
        try {
            KPIBackend kpiBackend = new KPIBackend(slug);
            List<Tuple> results = kpiBackend.select(kpiname,new Tuple(args));
            KPITable table = kpiBackend.getSchema(kpiname);
            JSONArray returnResults = new JSONArray();
            for (Tuple tuple : results) {
                JSONObject row = new JSONObject();
                for (KeyValue cell : tuple.getTuple()) {
                    for (KeyValue type : table.getKpi_schema().getColumnTypes()) {
                        if (cell.getKey().equals(type.getKey())) {
                            if (type.getValue().contains("integer")) {
                                if (cell.getValue() == "null") {
                                    cell.setValue("-1");
                                }
                                row.put(cell.getKey(), Integer.valueOf(cell.getValue()));
                            }
                            else if (type.getValue().contains("bigint")) {
                                if (cell.getValue() == "null") {
                                    cell.setValue("-1");
                                }
                                row.put(cell.getKey(), Long.valueOf(cell.getValue()));
                            }
                            else if (type.getValue().contains("json")) {
                                if (cell.getValue() == "null") {
                                    cell.setValue("{}");
                                }
                                if (cell.getValue().startsWith("["))
                                    row.put(cell.getKey(), new JSONArray(cell.getValue()));
                                else
                                    row.put(cell.getKey(), new JSONObject(cell.getValue()));
                            }
                            else {
                                if (cell.getValue() == "null") {
                                    cell.setValue("");
                                }
                                row.put(cell.getKey(), cell.getValue());
                            }
                        }
                    }
                }
                returnResults.put(row);
            }
            System.out.println(accepted);
            System.out.println(MediaType.valueOf(accepted));
            System.out.println(returnResults.toString());
            if(accepted != null) {
                MediaType mediaType = MediaType.valueOf(accepted);
                if (mediaType.equals(MediaType.APPLICATION_XML))
                    return ResponseEntity.ok(XML.toString(returnResults));
                else if (mediaType.equals(MediaType.APPLICATION_JSON))
                    return ResponseEntity.ok(returnResults.toString());
            }
            // service logic
            return ResponseEntity.ok(XML.toString(returnResults));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.noContent().build();
    }
}
