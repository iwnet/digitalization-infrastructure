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

import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.ExecutionEngine;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.ExecutionLanguage;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Recipe;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.RecipeArguments;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.LinkedList;

@RestController
@RequestMapping("recipes")
public class RecipeResource {
    private final static Logger LOGGER = Logger.getLogger(RecipeResource.class.getCanonicalName());

    /**
     * Recipe insert method
     * @param r the recipe to insert
     */
    @PostMapping(value = "{slug}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> insert(@PathVariable("slug") String slug,
                           @RequestBody Recipe r) {
        String details = "";

        try {

            List<ExecutionEngine> engines = ExecutionEngine.getEngines();

            boolean correctEngine = false;
            for (ExecutionEngine engine : engines) {
                if (engine.getId() == r.getEngineId()) {
                    correctEngine = true;
                }
            }

            List<ExecutionLanguage> languages = ExecutionLanguage.getLanguages();

            boolean correctLanguage = false;
            for (ExecutionLanguage language : languages) {
                if (language.getId() == r.getLanguageId()) {
                    correctLanguage = true;
                }
            }

            if (correctEngine && correctLanguage) {
                r.save(slug);

                details = Integer.toString(r.getId());

            }
            else {
                LOGGER.log(Level.WARNING, "Bad engine id or language id provided!");
                return new ResponseEntity<>("Could not create Recipe. Invalid json.", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Could not create Recipe.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(details);
    }

    /**
     * Method to create a Recipe from an existing shared recipe
     * @param recipeId the shared recipe id
     * @param args the recipe arguments
     */
    @PostMapping(value = "{slug}/{recipeId}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> insert(@PathVariable("slug") String slug,
                           @PathVariable("recipeId") int recipeId,
                           @RequestParam("name") String name,
                           @RequestBody RecipeArguments args) {
        String details = "";
        Recipe r;

        try {
            r = Recipe.createFromSharedRecipe(recipeId, name, args);
            r.save(slug);
            details = Integer.toString(r.getId());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Could not create Recipe.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(details);
    }

    /**
     * Uploads a binary for the specified recipe id.
     *
     * Given an existing `Recipe` object, this REST endpoint, receives a binary
     * and stores it in the configured storage backend. Then links the `Recipe` object to the
     * stored binary by running `recipe.setExecutablePath()`.
     *
     * TODO: Requires authentication/authorization.
     * TODO: Tests.
     *
     * @param slug          The repository slug.
     * @param recipeId      The `id` of the recipe object to which this binary corresponds.
     * @param recipeName    The name of the recipe's binary.
     * @param recipeBinary  The actual recipe binary.
     */
    @PutMapping(value = "{slug}/{recipeId}/{filename}",
            consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> upload(@PathVariable("slug") String slug,
                           @PathVariable("recipeId") int recipeId,
                           @PathVariable("filename") String recipeName,
                           InputStream recipeBinary)  {
        // Ensure a `Recipe` object with the given `id` exists.
        Recipe recipe;
        try {
            recipe = Recipe.getRecipeById(slug, recipeId);
        } catch (SQLException | SystemConnectorException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Upload recipe FAILED.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Ensure the storage location for the specified repository exists.
        try {
            Recipe.ensureStorageForSlug(slug);
        } catch (IOException | SystemConnectorException  e) {
            e.printStackTrace();
            return new ResponseEntity<>("Upload recipe FAILED.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Save the binary file to the specified location.
        String recipeFilename;
        try {
            recipeFilename = Recipe.saveRecipeForSlug(
                slug, recipeBinary, recipeName
            );
        } catch (IOException | SystemConnectorException  e) {
            e.printStackTrace();
            return new ResponseEntity<>("Upload recipe FAILED.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Update the `Recipe` object.
        recipe.setExecutablePath(recipeFilename);

        try {
            recipe.save(slug);
        } catch (SQLException | SystemConnectorException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Upload recipe FAILED.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok("");
    }

    /**
     * Returns all the registered recipes.
     */
    @GetMapping(value = "{slug}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Recipe> getRecipesView(@PathVariable("slug") String slug) {
        List<Recipe> recipes = new LinkedList<Recipe>();

        try {
            recipes = Recipe.getRecipes(slug);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recipes;
    }

    /**
     * Returns information about a specific recipe.
     */
    @GetMapping(value = "{slug}/{recipeId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Recipe getRecipeInfo(@PathVariable("slug") String slug,
                                @PathVariable("recipeId") Integer id) {
        Recipe recipe = null;

        try {
            recipe = Recipe.getRecipeById(slug, id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recipe;
    }

    /**
     * Delete a specific recipe.
     */
    @DeleteMapping(value = "{slug}/{recipeId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> deleteRecipe(@PathVariable("slug") String slug,
                                 @PathVariable("recipeId") Integer id) {
        try {
            Recipe.destroy(slug, id);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Could not delete Recipe.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok("");
    }
}
