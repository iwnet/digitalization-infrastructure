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

package gr.ntua.ece.cslab.iwnet.bda.datastore;

import gr.ntua.ece.cslab.iwnet.bda.common.storage.AbstractTestConnector;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;

import java.util.logging.Logger;

public class StorageBackendTest extends AbstractTestConnector {
    Logger LOGGER = Logger.getLogger(StorageBackendTest.class.getCanonicalName());

    @org.junit.Before
    public void setUp() throws SystemConnectorException {
        super.setUp();
    }

    @org.junit.After
    public void tearDown() throws SystemConnectorException {
        super.tearDown();
    }

    @org.junit.Test
    public void test() throws SystemConnectorException {

    }
}
