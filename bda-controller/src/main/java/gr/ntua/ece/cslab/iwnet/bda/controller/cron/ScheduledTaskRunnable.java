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

import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Job;

import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ScheduledTaskRunnable implements Runnable {

    private ScheduledTask scheduledTask;
    private int scheduledTime;

    public ScheduledTaskRunnable(String slug, Job job) {
        this.scheduledTask = new ScheduledTask(slug, job);
        this.scheduledTime = Integer.parseInt(job.getScheduleInfo());
    }


    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(this.scheduledTask, Calendar.getInstance().getTime(), TimeUnit.SECONDS.toMillis(scheduledTime));
    }
}
