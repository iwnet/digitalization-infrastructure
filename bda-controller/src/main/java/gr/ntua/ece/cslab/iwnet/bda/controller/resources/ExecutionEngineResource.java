package gr.ntua.ece.cslab.iwnet.bda.controller.resources;

import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.ExecutionEngine;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class ExecutionEngineResource {
    private final static Logger LOGGER = Logger.getLogger(ExecutionEngineResource.class.getCanonicalName());

    /**
     * Returns all the available execution engines.
     */
    @GetMapping(value = "/xengines", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<ExecutionEngine> getExecutionEngines() {
        List<ExecutionEngine> engines = new LinkedList<>();

        try {
            engines = ExecutionEngine.getEngines();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return engines;
    }
}
