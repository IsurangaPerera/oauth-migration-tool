/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

package org.wso2.carbon.oauth.migration.log.processor;

import org.testng.annotations.Test;
import org.wso2.carbon.oauth.migration.common.model.LogEntry;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class LogStatementProcessorTest {

    @Test
    public void testCall() {

        File file = new File(Objects.requireNonNull(getClass().getClassLoader()
                .getResource("log/audit.log")).getFile());
        LogStatementProcessor processor = new LogStatementProcessor(file);
        try {
            List<LogEntry> entryList = processor.call();
            assertEquals(entryList.size(), 3);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
