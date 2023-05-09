package gr.ntua.ece.cslab.iwnet.bda.controller.resources;

import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.ExecutionLanguage;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class ExecutionLanguageResource {
    private final static Logger LOGGER = Logger.getLogger(ExecutionLanguageResource.class.getCanonicalName());

    /**
     * Returns all the available execution languages.
     */
    @GetMapping(value = "/xlanguages", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<ExecutionLanguage> getSharedRecipes() {
        List<ExecutionLanguage> languages = new LinkedList<>();

        try {
            languages = ExecutionLanguage.getLanguages();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return languages;
    }
}
