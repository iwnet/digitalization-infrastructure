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

package gr.ntua.ece.cslab.iwnet.bda.controller.cron;

import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.DbInfo;
import gr.ntua.ece.cslab.iwnet.bda.controller.resources.JobResource;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Job;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CronJobScheduler {
    private final static Logger LOGGER = Logger.getLogger(JobResource.class.getCanonicalName());

    public static void init_scheduler() {
        Calendar.getInstance().add(Calendar.SECOND, 1);

        try {
            for (DbInfo info : DbInfo.getDbInfo()) {
                for (Job job : Job.getActiveJobs(info.getSlug())) {
                    if (job.getScheduleInfo() != null) {
                        schedule_job(info.getSlug(), job);

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.log(Level.WARNING, "Could not retrieve information to start defined cron jobs.");
        }
    }

    public static void schedule_job(String slug, Job job) {
        LOGGER.log(Level.INFO, "About to schedule cron job with id " + job.getId());
        Thread thread = new Thread(new ScheduledTaskRunnable(slug, job));
        thread.start();
    }
}
