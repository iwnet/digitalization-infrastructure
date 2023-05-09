package gr.ntua.ece.cslab.iwnet.bda.controller.resources;

import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Recipe;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class SharedRecipeResource {
    private final static Logger LOGGER = Logger.getLogger(SharedRecipeResource.class.getCanonicalName());

    /**
     * Returns all the registered shared recipes.
     */
    @GetMapping(value = "/sharedrecipes", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Recipe> getSharedRecipes() {
        List<Recipe> recipes = new LinkedList<>();

        try {
            recipes = Recipe.getSharedRecipes();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recipes;
    }
}
