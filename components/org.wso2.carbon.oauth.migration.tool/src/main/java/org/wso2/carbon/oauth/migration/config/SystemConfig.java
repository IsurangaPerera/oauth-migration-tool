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

import org.wso2.carbon.oauth.migration.common.runtime.ScannerConfig;
import org.wso2.carbon.oauth.migration.log.scanner.LogScanner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents System Configuration.
 *
 */
public class SystemConfig {

    private Map<String, ScannerConfig> processorConfigMap = new HashMap<>();
    private String processor;
    private Path workDir;
    private LogScanner logScanner;

    /**
     * Adds a processor configuration agianst the given processor name.
     *
     * @param processorName
     * @param processorConfig
     */
    public void addProcessorConfig(String processorName, ScannerConfig processorConfig) {

        processorConfigMap.put(processorName, processorConfig);
    }

    public Map<String, ScannerConfig> getProcessorConfigMap() {

        return Collections.unmodifiableMap(processorConfigMap);
    }

    /**
     * Add a processor name, effectively enables the processor.
     *
     * @param processor
     */
    public void addProcessor(String processor) {

        this.processor = processor;
    }

    public void addLogScanner(LogScanner logScanner) {

        this.logScanner = logScanner;
    }

    public LogScanner getLogScanner() {

        return logScanner;
    }

    /**
     * Returns the active processor.
     * @return
     */
    public String getProcessor() {

        return processor;
    }

    public Path getWorkDir() {

        return workDir;
    }

    public void setWorkDir(Path workDir) {

        this.workDir = workDir;
    }
}
