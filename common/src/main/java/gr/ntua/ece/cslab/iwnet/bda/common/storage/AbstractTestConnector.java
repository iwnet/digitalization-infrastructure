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

package gr.ntua.ece.cslab.iwnet.bda.common.storage;

import java.util.logging.Logger;

public abstract class AbstractTestConnector {
    private final static Logger LOGGER = Logger.getLogger(AbstractTestConnector.class.getCanonicalName());

    public AbstractTestConnector(){}

    public void setUp() throws SystemConnectorException {
        // SystemConnector.init("../conf/bdatest.properties");
    }

    public void tearDown() throws SystemConnectorException {
        // SystemConnector.getInstance().close();
    }
}
