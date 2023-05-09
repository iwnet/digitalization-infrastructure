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

import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.Connector;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("connectors")
public class ConnectorResource {
    private final static Logger LOGGER = Logger.getLogger(ConnectorResource.class.getCanonicalName());

    /**
     * Create new Connector.
     * @param connector a Connector description.
     */
    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> createNewConnector(@RequestBody Connector connector) {
        LOGGER.log(Level.INFO, connector.toString());

        String details = "";
        try {
            connector.save();
            details = Integer.toString(connector.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Could not register new Connector.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(details);
    }

    /**
     * Returns all the registered Connectors.
     */
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Connector> getConnectorsView() {
        List<Connector> connectors = new LinkedList<Connector>();

        try {
            connectors = Connector.getConnectors();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connectors;
    }

    /**
     * Returns information about a specific Connector.
     * @param id a Connector id.
     */
    @GetMapping(value = "{connectorId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Connector getConnectorInfo(@PathVariable("connectorId") Integer id) {
        Connector connector = null;

        try {
            connector = Connector.getConnectorInfoById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connector;
    }

    /**
     * Delete a Connector.
     * @param id a Connector id.
     */
    @DeleteMapping(value = "{connectorId}")
    public ResponseEntity<?> deleteConnector(@PathVariable("connectorId") Integer id) {

        try {
            Connector.destroy(id);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Could not delete Connector instance.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok("");
    }
}
