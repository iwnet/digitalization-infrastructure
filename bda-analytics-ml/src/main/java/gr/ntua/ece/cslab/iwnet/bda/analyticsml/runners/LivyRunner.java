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

package gr.ntua.ece.cslab.iwnet.bda.analyticsml.runners;

import gr.ntua.ece.cslab.iwnet.bda.common.Configuration;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.ExecutionLanguage;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.DbInfo;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.PostgresqlConnector;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Job;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.MessageType;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Recipe;

import org.json.JSONObject;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LivyRunner extends ArgumentParser implements Runnable {
    private final static Logger LOGGER = Logger.getLogger(LivyRunner.class.getCanonicalName());

    private String messageId;
    private String slug;
    private MessageType msgInfo;
    private Recipe recipe;
    private Job job;
    private static WebTarget resource;
    private static Configuration configuration;
    private String recipeClass;
    public String language;
    private String sessionId;

    public LivyRunner(Recipe recipe, MessageType msgInfo,
                      String messageId, Job job, String slug) throws Exception{

        this.messageId = messageId;
        this.msgInfo = msgInfo;
        this.slug = slug;
        this.recipe = recipe;
        this.job = job;
        configuration = Configuration.getInstance();

        Client client = ClientBuilder.newClient();
        resource = client.target(configuration.execEngine.getLivyURL());

        String[] recipe_export = recipe.getExecutablePath().split("\\.");
        recipe_export = recipe_export[recipe_export.length - 2].split("/");
        this.recipeClass = recipe_export[recipe_export.length - 1];

        try {
            this.language = ExecutionLanguage.getLanguageById(recipe.getLanguageId()).getName();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Getting the execution language has failed. Abort creating runner.");
            throw e;
        }

        this.sessionId = String.valueOf(job.getSessionId());
    }

    public String createSession(){
        String kind = null, dataLoaderLibrary = null, sparkFiles = null, sessionId;
        if (language.matches("python")){
            kind = "pyspark";
            dataLoaderLibrary = "hdfs:///RecipeDataLoader.py";
            sparkFiles = "spark.yarn.dist.pyFiles";
        }

        Invocation.Builder request = resource.path("/sessions").request();
        JSONObject data = new JSONObject();
        data.put("kind",kind);

        Map<String, String> classpath = new HashMap<>();
        List<String> files = new ArrayList<>();
        //files.add(recipe.getExecutablePath());
        files.add(dataLoaderLibrary);
        // For client mode with old configuration
        //data.put("files", files);
        //if (configuration.execEngine.getSparkConfJars() != null) {
        //    List<String> jars = new ArrayList<>();
        //    jars.add("file://" + configuration.execEngine.getSparkConfJars());
        //    data.put("jars", jars);
        //    classpath.put("spark.driver.extraClassPath", "file://" + configuration.execEngine.getSparkConfJars());
        //}
        // For cluster mode
        classpath.put(sparkFiles, String.join(",",files));
        String[] jar_links = configuration.execEngine.getSparkConfJars().split(",");
        List<String> jar_names = new ArrayList<>();
        for (String jar: jar_links) {
            String[] jar_name_list = jar.split("/");
            jar_names.add("hdfs:///"+jar_name_list[jar_name_list.length-1]);
        }	
        classpath.put("spark.jars", String.join(",", jar_names)+ ",local:///usr/local/hbase/lib/hbase-client-1.4.10.jar,local:///usr/local/hbase/lib/htrace-core-3.1.0-incubating.jar,local:///usr/local/hbase/lib/hbase-common-1.4.10.jar,local:///usr/local/hbase/lib/hbase-server-1.4.10.jar,local:///usr/local/hbase/lib/guava-12.0.1.jar,local:///usr/local/hbase/lib/hbase-protocol-1.4.10.jar,local:///usr/local/hbase/lib/metrics-core-2.2.0.jar");

        if (!classpath.isEmpty())
            data.put("conf", classpath);
        data.put("driverMemory", configuration.execEngine.getSparkConfDriverMemory());
        data.put("executorMemory", configuration.execEngine.getSparkConfExecutorMemory());
        data.put("executorCores", Integer.valueOf(configuration.execEngine.getSparkConfExecutorCores()));

        Response response = request.post(Entity.json(data.toString()));
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            LOGGER.log(Level.INFO,
                    "SUCCESS: Request to create session has been sent. Waiting for session to start..");
            sessionId = new JSONObject(response.readEntity(String.class)).get("id").toString();
        } else {
            LOGGER.log(Level.SEVERE,
                    "Request to create session has failed, got error: {0}",
                    response.getStatusInfo().getReasonPhrase());
            return null;
        }

        // Check that session is ready to receive job
        request = resource.path("/sessions/"+sessionId+"/state").request();
        response = request.get();
        String state = "not_started";
        while (!state.matches("idle")) {
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                response = request.get();
                state = new JSONObject(response.readEntity(String.class)).get("state").toString();
                if (state.matches("shutting_down") || state.matches("error") || state.matches("dead") || state.matches("success")){
                    LOGGER.log(Level.SEVERE, "Session is not alive, status: {0}", state);
                    return null;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                LOGGER.log(Level.SEVERE,
                        "Request to get session status has failed, got error: {0}",
                        response.getStatusInfo().getReasonPhrase());
                deleteSession(sessionId);
                return null;
            }
        }

        return sessionId;
    }

    public static void deleteSession(String sessionId){
        configuration = Configuration.getInstance();
        Client client = ClientBuilder.newClient();
        resource = client.target(configuration.execEngine.getLivyURL());
        Invocation.Builder request = resource.path("/sessions/"+sessionId).request();
        Response response = request.delete();
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            LOGGER.log(Level.INFO,
                    "SUCCESS: Session has been deleted.");
        } else {
            LOGGER.log(Level.SEVERE,
                    "Request to delete session has failed, got error: {0}",
                    response.getStatusInfo().getReasonPhrase());
        }
    }

    public static String getSessionState(String sessionId){
        configuration = Configuration.getInstance();
        Client client = ClientBuilder.newClient();
        resource = client.target(configuration.execEngine.getLivyURL());
        Invocation.Builder request = resource.path("/sessions/" + sessionId + "/state").request();
        Response response = request.get();
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            response = request.get();
            return new JSONObject(response.readEntity(String.class)).get("state").toString();
        } else {
            LOGGER.log(Level.SEVERE,
                    "Request to get session status has failed, got error: {0}",
                    response.getStatusInfo().getReasonPhrase());
            return "error";
        }
    }

    private List<String> buildDataLoadingPythonCode(DbInfo info) throws SQLException, SystemConnectorException {

        StringBuilder builder = new StringBuilder();
        builder.append("spark.sparkContext.addFile('").append(recipe.getExecutablePath()).append("'); ");
        builder.append("import RecipeDataLoader; import ").append(recipeClass).append("; ");

        StringBuilder arguments = new StringBuilder();

        if (msgInfo != null) {
            List<String> columns = msgInfo.getMessageColumns();
            builder.append(msgInfo.getName()).append("_input = RecipeDataLoader.fetch_from_eventlog_one(spark, '")
                    .append(info.getElDbname()).append("','")
                    .append(messageId).append("','")
                    .append(columns).append("'); ");
            arguments.append(msgInfo.getName()).append("_input");
        }

        List<String> dimension_tables = recipe.getArgs().getDimension_tables();
        for (String dimension_table: dimension_tables) {
            builder.append(dimension_table).append(" = RecipeDataLoader.fetch_from_master_data(spark, '")
                    .append(configuration.storageBackend.getDimensionTablesURL()).append(info.getDtDbname()).append("','")
                    .append(configuration.storageBackend.getDbUsername()).append("','")
                    .append(configuration.storageBackend.getDbPassword()).append("','")
                    .append(dimension_table).append("'); ");
            arguments.append(",").append(dimension_table);
        }

        List<String> eventlog_messages = recipe.getArgs().getMessage_types();
        for (String eventlog_message: eventlog_messages) {
            MessageType msg = MessageType.getMessageByName(this.slug, eventlog_message);
            List<String> columns = msg.getMessageColumns();
            builder.append(eventlog_message).append(" = RecipeDataLoader.fetch_from_eventlog(spark, '")
                    .append(info.getElDbname()).append("','")
                    .append(eventlog_message).append("','")
                    .append(columns).append("'); ");
            arguments.append(",").append(eventlog_message);
        }

        String args = arguments.toString();
        if (msgInfo == null) {
            args = arguments.toString().substring(1);
        }

        return Arrays.asList(builder.toString(), args);
    }

    private String buildRecipePythonCode(DbInfo info, String args) throws SQLException, SystemConnectorException {

        StringBuilder builder = new StringBuilder();
        StringBuilder arguments = new StringBuilder();
        arguments.append(args);

        List<String> other_args = recipe.getArgs().getOther_args();
        for (String arg: other_args)
            arguments.append(",'").append(arg).append("'");

        if (!(this.job.getDependJobId() == null))
            arguments.append(", result_").append(this.job.getDependJobId());

        builder.append("result_").append(this.job.getId()).append(" = ").append(recipeClass).append(".run(spark, ").append(arguments).append("); ");
        if (this.job.getResultStorage().equals("kpidb")){

            builder.append("RecipeDataLoader.save_result_to_kpidb('")
                    .append(PostgresqlConnector.getPostgresConnectionHost(configuration.kpiBackend.getDbUrl())).append("','")
                    .append(PostgresqlConnector.getPostgresConnectionPort(configuration.kpiBackend.getDbUrl())).append("','")
                    .append(info.getKpiDbname()).append("','")
                    .append(configuration.kpiBackend.getDbUsername()).append("','")
                    .append(configuration.kpiBackend.getDbPassword()).append("','")
                    .append(recipe.getName()).append("',");

            if (msgInfo != null) {
                List<String> columns = msgInfo.getMessageColumns();
                builder.append(msgInfo.getName()).append("_input,'")
                        .append(columns).append("',");
            } else {
                builder.append("'','',");
            }

            builder.append("result_").append(this.job.getId()).append(");");
        }
        else if (this.job.getResultStorage().equals("pubsub")) {
            String security = "SASL_PLAINTEXT";
            if (configuration.pubSubBackend.isSSLEnabled())
                security="SASL_SSL";

            builder.append("RecipeDataLoader.publish_result('")
                    .append(configuration.pubSubBackend.getBrokerUrl()).append("','")
                    .append(security).append("','")
                    .append(configuration.pubSubBackend.getClientId()).append("','")
                    .append(configuration.pubSubBackend.getClientSecret()).append("','")
                    .append(configuration.pubSubBackend.getAuthServerUri()).append("/token','")
                    .append(configuration.pubSubBackend.getProducerSSLCALocation()).append("','")
                    .append(configuration.pubSubBackend.getProducerSSLCertificateLocation()).append("','")
                    .append(configuration.pubSubBackend.getProducerkeyLocation()).append("','")
                    .append(info.getSlug()).append("','")
                    .append(recipe.getName()).append("_").append(job.getId())
                    .append("',result_").append(this.job.getId()).append(");");
        }

        // TODO : handle hdfs case

        return builder.toString();
    }

    @Override
    public void run() {
        DbInfo info;
        try {
            info = DbInfo.getDbInfoBySlug(slug);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String statementId, code = null;

        // Prepare code
        try {
            if (this.language.matches("python")){
                List<String> dataLoadingandArgs = buildDataLoadingPythonCode(info);
                String dataLoadingCode = dataLoadingandArgs.get(0);
                String dataArgs = dataLoadingandArgs.get(1);
                String recipeCode = buildRecipePythonCode(info, dataArgs);
                code = dataLoadingCode + recipeCode;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Building the code has failed. Aborting launching the job.");
            e.printStackTrace();
            return;
        }
        
        // Create session if it is required
        if (job.getJobType().matches("batch") && sessionId.matches("null") && job.getDependJobId() == null){
            LOGGER.log(Level.INFO,"Creating session for batch job..");
            sessionId = createSession();
        }
        else if (job.getJobType().matches("batch") && !sessionId.matches("null")) {
            if (job.getDependJobId() == null) {
                LOGGER.log(Level.SEVERE, "Found existing session for batch job with no parent job. This should never happen!");
                return;
            }
        }
        else if (job.getJobType().matches("streaming") && sessionId.matches("null")){
            LOGGER.log(Level.SEVERE,"Streaming job has no open session. This should never happen!");
            return;
        }
        if (sessionId==null || sessionId.matches("null"))
            return;

        // Launch job
        Invocation.Builder request = resource.path("/sessions/"+sessionId+"/statements").request();
        JSONObject data = new JSONObject();
        data.put("code",code);
        Response response = request.post(Entity.json(data.toString()));
        if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
            LOGGER.log(Level.INFO,
                    "SUCCESS: Job has been submitted.");
            statementId = new JSONObject(response.readEntity(String.class)).get("id").toString();
        } else {
            LOGGER.log(Level.SEVERE,
                    "Request to launch job has failed, got error: {0}",
                    response.getStatusInfo().getReasonPhrase());
            if (job.getJobType().matches("batch"))
                deleteSession(sessionId);
            return;
        }

        // Get job status
        request = resource.path("/sessions/"+sessionId+"/statements/"+statementId).request();
        response = request.get();
        String state = "waiting";
        JSONObject result = null;
        while (!state.matches("available")) {
            if (response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
                response = request.get();
                result = new JSONObject(response.readEntity(String.class));
                state = result.get("state").toString();
                if (state.matches("error") || state.contains("cancel")){
                    LOGGER.log(Level.SEVERE, "Job failed, status: {0}", result.get("output"));
                    if (job.getJobType().matches("batch"))
                        deleteSession(sessionId);
                    return;
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                LOGGER.log(Level.SEVERE,
                        "Request to get job status has failed, got error: {0}",
                        response.getStatusInfo().getReasonPhrase());
                if (job.getJobType().matches("batch"))
                    deleteSession(sessionId);
                return;
            }
        }
        if (new JSONObject(result.get("output").toString()).get("status").toString().equals("error")) {
            LOGGER.log(Level.SEVERE,
                    "Job error: {0}",
                    new JSONObject(result.get("output").toString()).get("traceback").toString());
        } else {
            String output = new JSONObject(new JSONObject(result.get("output").toString()).get("data").toString()).get("text/plain").toString();
            LOGGER.log(Level.INFO, "Job result: " + output);
        }

        // Delete session
        try {
            if (!(job.hasChildren(slug))) {
                if (job.getJobType().matches("batch"))
                    deleteSession(sessionId);
            }
            else {
                job.setSessionId(Integer.valueOf(sessionId));
                job.setChildrenSessionId(slug);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (SystemConnectorException e) {
            e.printStackTrace();
        }
    }
}
