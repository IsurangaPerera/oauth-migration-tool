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

package org.wso2.carbon.oauth.migration.sql.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oauth.migration.common.runtime.ModuleException;
import org.wso2.carbon.oauth.migration.sql.config.DataSourceConfig;
import org.wso2.carbon.oauth.migration.sql.exception.SQLModuleException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FederatedUserMgtDAO {

    private static final Logger log = LoggerFactory.getLogger(FederatedUserMgtDAO.class);

    private static final String AUTHZ_USER = "AUTHZ_USER";
    private static final String VARCHAR_TYPE = "VARCHAR";
    private DataSource dataSource;
    private DataSourceConfig dataSourceConfig;

    public FederatedUserMgtDAO() throws SQLModuleException {

        this.dataSourceConfig = DataSourceConfig.getInstance();
        try {
            dataSource = dataSourceConfig.getDatasource();
            if (log.isDebugEnabled()) {
                log.debug("Data source initialized with name: {}.", dataSource.getClass());
            }
        } catch (SQLModuleException e) {
            throw new SQLModuleException("Error occurred while initializing the data source.", e);
        }
    }

    public List<String> getActiveFederatedUsers() throws ModuleException {

        List<String> federatedUsers = new ArrayList<>();
        if (dataSource == null) {
            log.warn("No data source configured with name : "+ dataSourceConfig.getDatasourceName());
            return federatedUsers;
        }
        federatedUsers.addAll(getUsersWithActiveTokens());
        federatedUsers.addAll(getUsersWithActiveCodes());

        return federatedUsers.stream().distinct().collect(Collectors.toList());
    }

    public void revokeTokensAndCodes(List<String> federatedUsers) throws SQLModuleException {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                .REVOKE_FEDERATED_USER_TOKENS_QUERY)) {
            prepStmt.setArray(1, connection.createArrayOf(VARCHAR_TYPE, federatedUsers.toArray()));
            prepStmt.executeQuery();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .REVOKE_FEDERATED_USER_CODES_QUERY)) {
            prepStmt.setArray(1, connection.createArrayOf(VARCHAR_TYPE, federatedUsers.toArray()));
            prepStmt.executeQuery();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
    }

    public void migrateFederatedUsers() throws SQLModuleException {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .MIGRATE_TOKEN_QUERY)) {
            prepStmt.executeQuery();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .MIGRATE_AUTHORIZATION_CODE_QUERY)) {
            prepStmt.executeQuery();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
    }

    private List<String> getUsersWithActiveTokens() throws SQLModuleException {

        List<String> federatedUsers = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                    .ACTIVE_FEDERATED_USERS_WITH_TOKEN_QUERY);
            ResultSet resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                federatedUsers.add(resultSet.getString(AUTHZ_USER));
            }
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
        return federatedUsers;
    }

    private List<String> getUsersWithActiveCodes() throws SQLModuleException {

        List<String> federatedUsers = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                    .ACTIVE_FEDERATED_USERS_WITH_CODE_QUERY);
            ResultSet resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                federatedUsers.add(resultSet.getString(AUTHZ_USER));
            }
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
        return federatedUsers;
    }
}
