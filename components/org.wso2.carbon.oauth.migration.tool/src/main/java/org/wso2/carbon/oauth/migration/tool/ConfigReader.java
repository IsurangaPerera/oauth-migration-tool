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

package org.wso2.carbon.oauth.migration.tool;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oauth.migration.common.runtime.ScannerConfig;
import org.wso2.carbon.oauth.migration.common.util.CommonConstants;
import org.wso2.carbon.oauth.migration.config.ConfigConstants;
import org.wso2.carbon.oauth.migration.config.SystemConfig;
import org.wso2.carbon.oauth.migration.log.scanner.LogScanner;
import org.wso2.carbon.oauth.migration.log.scanner.impl.BasicLogScanner;
import org.wso2.carbon.oauth.migration.runtime.OAuthMigrationExecutionException;
import org.wso2.carbon.oauth.migration.runtime.ScannerConfigImpl;
import org.wso2.carbon.oauth.migration.sql.config.DataSourceConfig;
import org.wso2.carbon.oauth.migration.sql.exception.SQLModuleException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads the configuration from the main config file.
 *
 */
public class ConfigReader {

    private static final Logger log = LoggerFactory.getLogger(ConfigReader.class);

    private static Map<String, LogScanner> logScannerMap;
    private static ConfigReader configReader = new ConfigReader();

    public static ConfigReader getInstance() {

        return configReader;
    }

    private ConfigReader() {

        logScannerMap = new HashMap<>();
        logScannerMap.put(CommonConstants.BASIC_LOG_SCANNER, new BasicLogScanner());
    }

    /**
     * Reads the system configuration from the file given.
     *
     * @param file
     * @return SystemConfig
     * @throws OAuthMigrationExecutionException
     */
    public SystemConfig readSystemConfig(File file) throws OAuthMigrationExecutionException {

        SystemConfig systemConfig = new SystemConfig();
        JSONParser jsonParser = new JSONParser();

        try {
            Object parsedObject = jsonParser.parse(new FileReader(file));
            if (parsedObject instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) parsedObject;

                Object processors = jsonObject.get(ConfigConstants.CONFIG_ELEMENT_PROCESSOR);
                if (processors instanceof JSONObject) {
                    loadProcessors((JSONObject) processors, systemConfig);
                }

                Object dataSource = jsonObject.get(ConfigConstants.DATASOURCE);
                if (dataSource instanceof JSONObject) {
                    loadDataSource((JSONObject)dataSource);
                }

                Object logConfig = jsonObject.get(ConfigConstants.LOG_CONFIG);
                if (logConfig instanceof JSONObject) {
                    loadLogConfiguration((JSONObject) logConfig, systemConfig);
                }
            }
        } catch (IOException e) {
            throw new OAuthMigrationExecutionException(
                    "Could not read the config files related to : " + file.getAbsolutePath(), e);
        } catch (ParseException e) {
            throw new OAuthMigrationExecutionException("Could not parse config files related to: " +
                    file.getAbsolutePath(), e);
        }
        return systemConfig;
    }

    private void loadLogConfiguration(JSONObject logConfig, SystemConfig systemConfig) {

        Object processor = logConfig.get(ConfigConstants.CONFIG_ELEMENT_PROCESSOR);
        Object path = logConfig.get(ConfigConstants.LOG_FILE_PATH);
        Object logRegEx = logConfig.get(ConfigConstants.LOG_FILE_NAME_REGEX);
        Object logStatementRegEx = logConfig.get(ConfigConstants.LOG_STATEMENT_PATTERN);
        if (processor instanceof String && path instanceof String && logRegEx instanceof String
                && logStatementRegEx instanceof String) {
            String processorName = (String) processor;
            if (systemConfig.getProcessor().equalsIgnoreCase(processorName)) {
                ScannerConfig config = new ScannerConfigImpl();
                config.setLogFilePath((String)path);
                config.setLogFileRegEx((String) logRegEx);
                config.setLogStatementPattern((String) logStatementRegEx);
                LogScanner logScanner = logScannerMap.get(processor);
                logScanner.setScannerConfig(config);
                systemConfig.addLogScanner(logScanner);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("The processor : {} is not enabled", processor);
                }
            }
        }
    }

    private void loadProcessors(JSONObject processor, SystemConfig systemConfig) {

        Object processorType = processor.get(ConfigConstants.PROCESSOR_TYPE);
        if (processorType instanceof String) {
            systemConfig.addProcessor((String) processorType);
        }
    }

    private void loadDataSource(JSONObject dataSource)
            throws OAuthMigrationExecutionException {

        Object datasourcePath = dataSource.get(ConfigConstants.DATASOURCE_PATH);
        Object datasourceName = dataSource.get(ConfigConstants.DATASOURCE_NAME);
        if (datasourcePath instanceof String && datasourceName instanceof String) {
            Path path = Paths.get((String) datasourcePath);
            DataSourceConfig.getInstance().setDatasourceName((String) datasourceName);
            try {
                DataSourceConfig.getInstance().readProcessorConfig(path);
            } catch (SQLModuleException e) {
                throw new OAuthMigrationExecutionException(e.getMessage());
            }
        }
    }
}
