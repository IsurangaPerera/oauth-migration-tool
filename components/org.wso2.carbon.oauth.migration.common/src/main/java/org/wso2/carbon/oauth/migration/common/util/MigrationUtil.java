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

package org.wso2.carbon.oauth.migration.common.util;

import org.wso2.carbon.oauth.migration.log.model.LogEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MigrationUtil {

    public static List<String> filterLogEntryList(List<LogEntry> entryList) {

        Map<String, Integer> entryMap = new HashMap<>();

        for(LogEntry entry : entryList) {
            if (entryMap.containsKey(entry.getAuthenticatedUser())) {
                entryMap.put(entry.getAuthenticatedUser(), entryMap.get(entry.getAuthenticatedUser())+1);
            } else {
                entryMap.put(entry.getAuthenticatedUser(), 1);
            }
        }
        return entryMap.entrySet().stream().filter(entry -> entry.getValue() > 3L).map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
