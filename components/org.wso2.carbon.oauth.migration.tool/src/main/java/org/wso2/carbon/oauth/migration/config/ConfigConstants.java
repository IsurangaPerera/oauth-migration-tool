/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.oauth.migration.config;

/**
 * Configuration constants.
 */
public class ConfigConstants {

    public static final String PROCESSOR_TYPE = "type";
    public static final String CONFIG_ELEMENT_PROCESSOR = "processor";
    public static final String LOG_FILE_PATH = "log-file-path";
    public static final String LOG_FILE_NAME_REGEX = "log-file-name-regex";
    public static final String DATASOURCE = "datasource";
    public static final String DATASOURCE_PATH = "datasource-path";
    public static final String LOG_CONFIG = "log-config";
    public static final String DATASOURCE_NAME = "datasource-name";
    public static final String LOG_STATEMENT_PATTERN = "log-statement-pattern";

    /**
     * Prevents instantiation.
     */
    private ConfigConstants() {

    }
}
