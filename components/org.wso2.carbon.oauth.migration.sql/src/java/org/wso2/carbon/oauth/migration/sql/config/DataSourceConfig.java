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

package org.wso2.carbon.oauth.migration.sql.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.datasource.core.DataSourceManager;
import org.wso2.carbon.datasource.core.beans.CarbonDataSource;
import org.wso2.carbon.oauth.migration.sql.exception.SQLModuleException;

import javax.sql.DataSource;

/**
 * Represents a data source configuration.
 */
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private String dataSourceName;
    private DataSourceManager dataSourceManager;

    /**
     * Constructs the config with given datasource name and the given datasource manager.
     *
     * @param dataSourceName Name of the data source.
     * @param dataSourceManager Data source manager instance.
     */
    public DataSourceConfig(String dataSourceName, DataSourceManager dataSourceManager) {
        
        this.dataSourceName = dataSourceName;
        this.dataSourceManager = dataSourceManager;
    }

    /**
     * Returns the datasource for the given configuration, given by current datasource name.
     *
     * @return Instance of Datasource.
     * @throws SQLModuleException Error while finding the datasource.
     */
    public DataSource getDatasource() throws SQLModuleException {
        
        if (dataSourceManager.getDataSourceRepository() != null) {
            CarbonDataSource carbonDataSource = dataSourceManager.getDataSourceRepository()
                    .getDataSource(dataSourceName);
            if (carbonDataSource != null) {
                return (DataSource) carbonDataSource.getDataSourceObject();
            } else {
                log.error("Could not find a datasource for the name : " + dataSourceName);
            }
        } else {
            throw new SQLModuleException("Datasource manager is not initialized.");
        }
        return null;
    }
}
