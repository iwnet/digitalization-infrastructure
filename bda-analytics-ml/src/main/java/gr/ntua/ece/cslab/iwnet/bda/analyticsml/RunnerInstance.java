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

package gr.ntua.ece.cslab.iwnet.bda.analyticsml;

import gr.ntua.ece.cslab.iwnet.bda.analyticsml.runners.LivyRunner;
import gr.ntua.ece.cslab.iwnet.bda.analyticsml.runners.RunnerFactory;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.ExecutionEngine;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.DbInfo;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Recipe;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Job;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.MessageType;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RunnerInstance {
    private final static Logger LOGGER = Logger.getLogger(RunnerInstance.class.getCanonicalName()+" [" + Thread.currentThread().getName() + "]");
    private String slug;
    private MessageType msgInfo;
    private Job job;
    private Recipe recipe;
    public ExecutionEngine engine;

    public RunnerInstance(String slug, String messageType) throws Exception {
        this.slug = slug;

        try {
            msgInfo = MessageType.getMessageByName(slug, messageType);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Message type not found.");
        }
        try {
            // TODO: handle multiple jobs related to a single message
            job = Job.getJobByMessageId(slug, msgInfo.getId());
        } catch (SQLException e) {
            throw new Exception("No job found for message " + messageType + ".");
        }

        recipe = Recipe.getRecipeById(slug, job.getRecipeId());

        try {
            engine = ExecutionEngine.getEngineById(recipe.getEngineId());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Execution engine not found.");
        }
    }

    public RunnerInstance(String slug, int jobId) throws Exception {
        this.slug = slug;
        this.msgInfo = null;

        job = Job.getJobById(slug, jobId);
        recipe = Recipe.getRecipeById(slug, job.getRecipeId());

        try {
            engine = ExecutionEngine.getEngineById(recipe.getEngineId());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Execution engine not found.");
        }
    }

    public void loadLivySession(Job j, Recipe r, MessageType m, String messageId){
        LOGGER.log(Level.INFO, "Creating session for " + j.getName() + " job.");
        new Thread(() -> {
            try {
                LivyRunner runner = new LivyRunner(r, m, messageId, j, slug);
                String sessionId = runner.createSession();
                if (sessionId==null)
                    return;
                // TODO: Load dataframes in session
                Job.storeSession(slug, j.getId(), Integer.valueOf(sessionId));
                LOGGER.log(Level.INFO, "Session created.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void deleteLivySession(String slug, Job j){
        LOGGER.log(Level.INFO, "Destroying session with id " + j.getSessionId());
        //new Thread(() -> {
            try {
                LivyRunner.deleteSession(String.valueOf(j.getSessionId()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Job.storeSession(slug, j.getId(), null);
            } catch (SQLException | SystemConnectorException throwables) {
                throwables.printStackTrace();
            }

        //}).start();
    }

    public static void reloadLivySessions(){
        try {
            for (DbInfo info : DbInfo.getDbInfo()) {
                List<Job> jobs = Job.getActiveJobs(info.getSlug());
                for (Job j: jobs){
                    if (j.getJobType().matches("streaming") && j.getDependJobId() == null){
                        Recipe r = Recipe.getRecipeById(info.getSlug(), j.getRecipeId());
                        if (ExecutionEngine.getEngineById(r.getEngineId()).getName().matches("spark-livy")){
                            if (j.getSessionId()!=null) {
                                String state = LivyRunner.getSessionState(String.valueOf(j.getSessionId()));
                                if (state.matches("recovering") || state.matches("error") || state.matches("dead")) {
                                    LOGGER.log(Level.INFO, "Found zombie session for job "+j.getName());
                                    deleteLivySession(info.getSlug(), j);
                                    j.setSessionId(null);
                                }
                            }
                            if (j.getSessionId()==null) {
                                MessageType msg = MessageType.getMessageById(info.getSlug(), j.getMessageTypeId());
                                RunnerInstance runner = new RunnerInstance(info.getSlug(), msg.getName());
                                runner.loadLivySession(j, r, msg, String.valueOf(j.getMessageTypeId()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(String messageId) throws Exception {

        LOGGER.log(Level.INFO, "Launching " + job.getName() + " recipe.");
        Runnable runner = RunnerFactory.getInstance().getRunner(recipe, engine, msgInfo, messageId, job, this.slug);
        Thread thread = new Thread(runner);

        thread.start();
    }

    public void schedule(){
        // TODO: create a new cron job
        new Thread(() -> {

        }).start();
    }

    public static void unschedule(){
        // TODO: delete cron job
        new Thread(() -> {

        }).start();
    }
}
