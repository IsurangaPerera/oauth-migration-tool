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

import org.wso2.carbon.oauth.migration.common.util.CommonConstants;
import org.wso2.carbon.oauth.migration.log.model.LogEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogStatementProcessor implements Callable<List<LogEntry>> {

    private File file;
    private Pattern extractionPattern;

    public LogStatementProcessor(File file) {

        this.file = file;
        this.extractionPattern = Pattern.compile("((\"?[\\w]+\"?)\\s:\\s(\"?[\\w]+(@\\w+.\\w+)?=?\"?))");
    }

    @Override
    public List<LogEntry> call() throws Exception {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<LogEntry> entryList = new ArrayList<>();
        String logEntry = reader.readLine();
        while (logEntry != null) {
            if (isFederatedLogin(logEntry)) {
                entryList.add(buildLogEntry(logEntry));
            }
            logEntry = reader.readLine();
        }
        return entryList;
    }

    private LogEntry buildLogEntry(String logEntry) {

        Matcher matcher = extractionPattern.matcher(logEntry);
        LogEntry entry = new LogEntry();
        while(matcher.find()) {
            String[] entryMap = matcher.group(1).replace("\"", "").split(" : ");
            if (CommonConstants.AUTHENTICATED_USER.equalsIgnoreCase(entryMap[0])) {
                entry.setAuthenticatedUser(entryMap[1]);
            }
        }
        return entry;
    }

    private boolean isFederatedLogin(String logEntry) {

        return logEntry.contains(CommonConstants.LOGIN_ACTION);
    }
}
