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

import gr.ntua.ece.cslab.iwnet.bda.analyticsml.RunnerInstance;
import gr.ntua.ece.cslab.iwnet.bda.common.Configuration;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnector;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.ExecutionEngine;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.ExecutionLanguage;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.DbInfo;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.HDFSConnector;
import gr.ntua.ece.cslab.iwnet.bda.datastore.StorageBackend;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import com.google.common.base.Splitter;
import org.apache.commons.io.IOUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class holds the REST API of the datastore object.
 */
@RestController
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
@RequestMapping("datastore")
public class DatastoreResource {
    private final static Logger LOGGER = Logger.getLogger(DatastoreResource.class.getCanonicalName());

    /**
     * Perform initial upload of libraries and shared recipes in HDFS and populate
     * shared_recipes, execution_engines and execution_languages tables.
     */
    @PostMapping(value = "init", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> initialUploads() {

        Configuration configuration = Configuration.getInstance();
        ClassLoader classLoader = RunnerInstance.class.getClassLoader();

        try {
            ExecutionLanguage lang = new ExecutionLanguage("python");
            lang.save();
        } catch (Exception e) {
            e.printStackTrace();
            new ResponseEntity<>("Failed to populate execution languages table.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            ExecutionEngine eng = new ExecutionEngine("python3", "/usr/bin/python3", true, "{}");
            eng.save();
            eng = new ExecutionEngine("spark-livy", "http://bda-livy:8998", false, "{}");
            eng.save();
        } catch (Exception e) {
            e.printStackTrace();
            new ResponseEntity<>("Failed to populate execution engines table.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (configuration.execEngine.getRecipeStorageType().startsWith("hdfs")) {
            // Use HDFS storage for recipes and libraries.
            HDFSConnector connector = null;
            try {
                connector = (HDFSConnector)
                        SystemConnector.getInstance().getHDFSConnector();
            } catch (SystemConnectorException e) {
                e.printStackTrace();
                new ResponseEntity<>("Failed to get HDFS connector.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            org.apache.hadoop.fs.FileSystem fs = connector.getFileSystem();

            InputStream fileInStream = classLoader.getResourceAsStream("RecipeDataLoader.py");

            byte[] recipeBytes = new byte[0];
            try {
                recipeBytes = IOUtils.toByteArray(fileInStream);

                fileInStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                new ResponseEntity<>("Failed to create stream of Recipes library file.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            // Create HDFS file path object.
            org.apache.hadoop.fs.Path outputFilePath =
                    new org.apache.hadoop.fs.Path("/RecipeDataLoader.py");

            // Write to HDFS.
            org.apache.hadoop.fs.FSDataOutputStream outputStream = null;
            try {
                outputStream = fs.create(
                        outputFilePath
                );
            } catch (IOException e) {
                e.printStackTrace();
                new ResponseEntity<>("Failed to create Recipes library file in HDFS.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            try {
                outputStream.write(recipeBytes);
            } catch (IOException e) {
                e.printStackTrace();
                new ResponseEntity<>("Failed to upload Recipes library to HDFS.", HttpStatus.INTERNAL_SERVER_ERROR);
            } finally {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                String path = System.getProperty("user.dir").replaceFirst("bda-controller","bda-analytics-ml/src/main/resources/shared_recipes/");
                org.apache.hadoop.fs.Path folderToUpload = new org.apache.hadoop.fs.Path(path);

                // Create HDFS file path object.
                outputFilePath = new org.apache.hadoop.fs.Path("/");
                fs.copyFromLocalFile(folderToUpload, outputFilePath);
            } catch (Exception e) {
                e.printStackTrace();
                new ResponseEntity<>("Failed to upload shared recipes to HDFS.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String[] jar_links = configuration.execEngine.getSparkConfJars().split(",");
            for (String link: jar_links) {

                try {
                    fileInStream = new URL(link).openStream();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Spark jar clients download failed!! Please check the URLs");
                }

                byte[] jarBytes = new byte[0];
                try {
                    jarBytes = IOUtils.toByteArray(fileInStream);

                    fileInStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    new ResponseEntity<>("Failed to create Spark jar stream.", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                String[] jar_name = link.split("/");
                outputFilePath =
                        new org.apache.hadoop.fs.Path("/" + jar_name[jar_name.length - 1]);

                try {
                    outputStream = fs.create(
                            outputFilePath
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                    new ResponseEntity<>("Failed to create Spark jar file in HDFS.", HttpStatus.INTERNAL_SERVER_ERROR);
                }

                try {
                    outputStream.write(jarBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                    new ResponseEntity<>("Failed to upload Spark jars to HDFS.", HttpStatus.INTERNAL_SERVER_ERROR);
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                RecipeArguments args = new RecipeArguments();
                List<String> args_list = new LinkedList<>();
                args_list.add("labelColumnName");
                args_list.add("maxIter");
                args_list.add("regParam");
                args_list.add("elasticNetParam");
                args.setOther_args(args_list);
                List<String> input_df_args_list = new LinkedList<>();
                input_df_args_list.add("eventLogMessageType1");
                args.setMessage_types(input_df_args_list);

                Recipe r = new Recipe("Linear Regression model train", "Simple regression training algorithm using Spark MLlib",
                        1, "hdfs:///shared_recipes/linear_regression_train.py", 2, args);
                r.save_as_shared();

                r = new Recipe("Binomial Logistic Regression model train", "Simple binary classification training algorithm using Spark MLlib",
                        1, "hdfs:///shared_recipes/binomial_logistic_regression_train.py", 2, args);
                r.save_as_shared();

                args = new RecipeArguments();
                r = new Recipe("Linear Regression prediction", "Simple regression prediction using Spark MLlib",
                        1, "hdfs:///shared_recipes/linear_regression_predict.py", 2, args);
                r.save_as_shared();
                r = new Recipe("Binomial Logistic Regression prediction", "Simple binary classification prediction using Spark MLlib",
                        1, "hdfs:///shared_recipes/binomial_logistic_regression_predict.py", 2, args);
                r.save_as_shared();

            } catch (Exception e) {
                e.printStackTrace();
                new ResponseEntity<>("Failed to populate shared recipes table.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else {
            // Upload any shared recipes for local execution
        }

        return ResponseEntity.ok("");
    }

    /**
     * Create new repository databases/schemas/tables.
     * @param info a repository description.
     */
    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> createNewRepository(@RequestBody DbInfo info) {
        LOGGER.log(Level.INFO, info.toString());

        String details = "";

        try {
            info.save();
            details = Integer.toString(info.getId());
        } catch (Exception e) {
            e.printStackTrace();
            new ResponseEntity<>("Could not register new repository.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            StorageBackend.createNewDB(info);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.INFO, "Clearing repository registry and databases after failure.");
            try {
                StorageBackend.destroyDBs(info);
            } catch (Exception e1) {
            }
            try {
                DbInfo.destroy(info.getId());
            } catch (Exception e1) {
                e1.printStackTrace();
                LOGGER.log(Level.SEVERE, "Could not clear registry, after databases creation failed!");
            }
            new ResponseEntity<>("Could not create new repository.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(details);
    }

    /**
     * Returns all the registered repositories.
     */
    @GetMapping(
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<DbInfo> getDbInfoView() {
        List<DbInfo> dbInfos = new LinkedList<DbInfo>();

        try {
            dbInfos = DbInfo.getDbInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dbInfos;
    }

    /**
     * Returns information about a specific repository.
     * @param id the repository registry id.
     */
    @GetMapping(value = "{repoId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public DbInfo getInfo(@PathVariable("repoId") Integer id) {
        DbInfo info = null;

        try {
            info = DbInfo.getDbInfoById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }

    /**
     * Destroy an repository databases/schemas/tables.
     * @param repoId a repository id.
     */
    @DeleteMapping(value = "{repoId}")
    public ResponseEntity<?> destroyRepository(@PathVariable("repoId") Integer repoId) {

        DbInfo info = null;

	// TODO: unschedule jobs and destroy live sessions
        try {
            info = DbInfo.getDbInfoById(repoId);
            StorageBackend.destroyDBs(info);
        } catch (Exception e) {
            e.printStackTrace();
            new ResponseEntity<>("Could not destroy repository databases.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            DbInfo.destroy(repoId);
        } catch (Exception e) {
            e.printStackTrace();
            new ResponseEntity<>("Could not destroy repository.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok("");
    }

    /**
     * Message insertion method
     * @param m the message to insert
     */
    @PostMapping(value = "{slug}",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> insert(@PathVariable("slug") String slug,
                           @RequestBody Message m) {
        LOGGER.log(Level.INFO, m.toString());
        try {
            new StorageBackend(slug).insert(m);
        } catch (Exception e) {
            e.printStackTrace();
            new ResponseEntity<>("Could not insert new message.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("");
    }


    /**
     * Responsible for datastore bootstrapping
     * @param masterData the schema of the dimension tables along with their content
     * @return a response for the status of bootstrapping
     */
    @PostMapping(value = "{slug}/boot",
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> bootstrap(@PathVariable("slug") String slug,
                              @RequestBody MasterData masterData) {
        try {
            new StorageBackend(slug).init(masterData);
        } catch (Exception e) {
            e.printStackTrace();
            new ResponseEntity<>("Could not insert master data.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok("");
    }

    /**
     * Returns the filtered content of a given dimension table
     * @param tableName is the name of the table to search
     * @param filters contains the names and values of the columns to be filtered
     * @return the selected content of the dimension table
     */
    @GetMapping(value = "{slug}/dtable", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Tuple> getTable(
            @RequestParam("tableName") String tableName,
            @RequestParam("filters") String filters,
            @PathVariable("slug") String slug
    ) {
        try {
            HashMap<String, String> mapfilters;
            if(filters != null && !filters.isEmpty()) {
                Map<String, String> map = Splitter.on(';').withKeyValueSeparator(":").split(filters);
                mapfilters = new HashMap<String, String>(map);
            }
            else {
                mapfilters = new HashMap<String, String>();
            }
            return new StorageBackend(slug).select(tableName, mapfilters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }


    /**
     * Returns the schema of all dimension tables
     * @return a list of the dimension tables schemas
     */
    @GetMapping(value = "{slug}/schema", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<DimensionTable> getSchema(@PathVariable("slug") String slug) {
        try {
            List<String> tables = new StorageBackend(slug).listTables();
            List<DimensionTable> res = new LinkedList<>();
            for (String table: tables){
                DimensionTable schema = new StorageBackend(slug).getSchema(table);
                LOGGER.log(Level.INFO, "Table: " +table + ", Columns: "+ schema.getSchema().getColumnNames());
                res.add(schema);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new LinkedList<>();
    }

    /**
     * Returns the last entries (i.e., messages) stored in the event log.
     * @param type one of days, count
     * @param n the number of days/messages to fetch
     * @return the denormalized messages
     */
    @GetMapping(value = "{slug}/entries", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Tuple> getEntries(@RequestParam("type") String type,
                                  @RequestParam("n") Integer n,
                                  @PathVariable("slug") String slug) {
        try {
            return new StorageBackend(slug).fetch(type,n);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    /**
     * Returns the filtered entries (i.e., messages) stored in the event log.
     * @param filters contains the names and values of the columns to be filtered
     * @return the denormalized messages
     */
    @GetMapping(value = "{slug}/select", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Tuple> getSelectedEntries(
            @RequestParam("filters") String filters,
            @PathVariable("slug") String slug
    ) {
        try {
            Map<String,String> map= Splitter.on(';').withKeyValueSeparator(":").split(filters);
            HashMap<String, String> mapfilters = new HashMap<String, String>(map);
            return new StorageBackend(slug).select(mapfilters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }
}

