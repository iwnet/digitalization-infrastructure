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

package gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors;

import java.io.*;

public class LocalFSConnector implements Connector {
    private String FS;

    // This method creates the filesystem folder using the 'FS' parameter.
    // If this folder exists, it should be initially empty (before the bootstraping).
    public LocalFSConnector(String FS, String username, String password){
        this.FS = FS;

        File fs = new File(FS);
        if (!fs.exists())
            fs.mkdir();
    }

    public String getFS() {
        return FS;
    }

    public void close(){
        throw new UnsupportedOperationException();
    }
}
