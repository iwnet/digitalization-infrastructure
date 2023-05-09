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

import java.util.*;

import gr.ntua.ece.cslab.iwnet.bda.common.storage.beans.ExecutionEngine;
import gr.ntua.ece.cslab.iwnet.bda.datastore.beans.Recipe;

public class LocalRunner extends ArgumentParser implements Runnable {

    String messageId;
    String slug;
    Recipe recipe;
    ExecutionEngine engine;

    public LocalRunner(Recipe recipe,
                    ExecutionEngine engine,
                    String messageId,
                    String slug) {
        this.recipe = recipe;
        this.engine = engine;
        this.messageId = messageId;
        this.slug = slug;

    }

    public void run() {
        try {
            if (engine.getArgs().length() != 0) {
                // TODO: Add code to support engine arguments
            }

            ProcessBuilder pb = new ProcessBuilder(Arrays.asList(
                    engine.getEngine_path(), recipe.getExecutablePath(), messageId));

            //File out = new File("/results/" + recipe.getName() + ".out");
            //pb.redirectError(ProcessBuilder.Redirect.to(out));
            //pb.redirectOutput(ProcessBuilder.Redirect.to(out));
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
