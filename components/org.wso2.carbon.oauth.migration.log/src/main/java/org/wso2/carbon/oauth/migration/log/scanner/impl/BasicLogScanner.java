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

package org.wso2.carbon.oauth.migration.log.scanner.impl;

import org.wso2.carbon.oauth.migration.log.model.LogEntry;
import org.wso2.carbon.oauth.migration.log.processor.LogStatementProcessor;
import org.wso2.carbon.oauth.migration.log.scanner.LogScanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BasicLogScanner implements LogScanner {

    private String logDirectoryPath;

    @Override
    public void processAuditLogs() {

        crawlDirectoryAndProcessFiles(new File(logDirectoryPath));

    }

    @Override
    public void setLogDirectoryPath(String logDirectoryPath) {

        this.logDirectoryPath = logDirectoryPath;
    }

    @Override
    public String getLogDirectoryPath() {

        return logDirectoryPath;
    }

    private List<LogEntry> crawlDirectoryAndProcessFiles(File directory) {

        List<LogEntry> logEntryList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<LogStatementProcessor> calls = new ArrayList<>();

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                crawlDirectoryAndProcessFiles(file);
            } else {
                calls.add(new LogStatementProcessor(file));
            }
        }
        try {
            List<Future<List<LogEntry>>> futures = executor.invokeAll(calls);
            for(Future future : futures) {
                logEntryList.addAll((List<LogEntry>)future.get());
            }
        } catch (InterruptedException ignored) {

        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return logEntryList;
    }
}
