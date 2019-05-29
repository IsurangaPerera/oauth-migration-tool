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

import org.apache.commons.lang.StringUtils;
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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FederatedUserMgtDAO {

    private static final Logger log = LoggerFactory.getLogger(FederatedUserMgtDAO.class);

    private static final String AUTHZ_USER = "AUTHZ_USER";
    private static final String TOKEN_ID = "TOKEN_ID";
    private static final String CODE_ID = "CODE_ID";
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

    public List<String[]> revokeAllTokens() throws SQLModuleException {

        List<String[]> revokedTokens = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                    .ACTIVE_FEDERATED_USER_ENTRY_TOKEN_QUERY);
            ResultSet resultSet = prepStmt.executeQuery()) {
            while (resultSet.next()) {
                String[] entry = {resultSet.getString(TOKEN_ID), resultSet.getString(AUTHZ_USER)};
                revokedTokens.add(entry);
            }
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .REVOKE_ALL_FEDERATED_USER_TOKENS_QUERY)) {
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        return revokedTokens;
    }

    public List<String[]> revokeAllAuthorizationCodes() throws SQLModuleException {

        List<String[]> revokedCodes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .ACTIVE_FEDERATED_USER_ENTRY_CODE_QUERY);
             ResultSet resultSet = prepStmt.executeQuery()) {
            while (resultSet.next()) {
                String[] entry = {resultSet.getString(CODE_ID), resultSet.getString(AUTHZ_USER)};
                revokedCodes.add(entry);
            }
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .REVOKE_ALL_FEDERATED_USER_CODES_QUERY)) {
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        return revokedCodes;
    }

    public void revokeTokensAndCodes(List<String> federatedUsers) throws SQLModuleException {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(processQueryContainingList
                     (federatedUsers.size(), DAOConstants.REVOKE_FEDERATED_USER_TOKENS_QUERY, "#"))) {
            for(int i = 1; i <= federatedUsers.size(); ++i) {
                prepStmt.setString(i, federatedUsers.get(i-1));
            }
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(processQueryContainingList
                     (federatedUsers.size(), DAOConstants.REVOKE_FEDERATED_USER_CODES_QUERY, "#"))) {
            for(int i = 1; i <= federatedUsers.size(); ++i) {
                prepStmt.setString(i, federatedUsers.get(i-1));
            }
            prepStmt.executeQuery();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
    }

    public void migrateFederatedUsers() throws SQLModuleException {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .MIGRATE_TOKEN_QUERY)) {
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .MIGRATE_AUTHORIZATION_CODE_QUERY)) {
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
    }

    public List<Long> getTokenExpirationTime() throws SQLModuleException {

        List<Long> tokenExpirationTimeList = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                    .TOKEN_EXPIRATION_TIME_QUERY);
            ResultSet resultSet = prepStmt.executeQuery();
            while(resultSet.next()) {
                Timestamp createdTime = resultSet.getTimestamp(DAOConstants.TIME_CREATED);
                ZonedDateTime utcDateTime = ZonedDateTime.ofInstant(createdTime.toInstant(), ZoneId.of("UTC"));
                LocalDateTime localDateTime = utcDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();


                long validityPeriod = resultSet.getLong(DAOConstants.VALIDITY_PERIOD);
                LocalDateTime expirationTime = localDateTime.plusSeconds(validityPeriod/1000);

                if(expirationTime.isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
                    tokenExpirationTimeList.add(LocalDateTime.now(ZoneOffset.UTC).until(expirationTime,
                            ChronoUnit.DAYS));
                }
            }
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
        return tokenExpirationTimeList;
    }

    public List<String[]> revokeAllTokensAfter(String date) throws SQLModuleException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<String[]> revokedTokens = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .ACTIVE_FEDERATED_USER_CREATED_AFTER_TOKEN_QUERY)) {
            Date d = dateFormat.parse(date);
            prepStmt.setTimestamp(1, new Timestamp(d.getTime()));
            ResultSet resultSet = prepStmt.executeQuery();
            while (resultSet.next()) {
                String[] entry = {resultSet.getString(TOKEN_ID), resultSet.getString(AUTHZ_USER)};
                revokedTokens.add(entry);
            }
        } catch (SQLException | ParseException e) {
            throw new SQLModuleException(e);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .REVOKE_ALL_FEDERATED_USER_TOKENS_AFTER_QUERY)) {
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        return revokedTokens;
    }

    public List<String[]> revokeAllAuthorizationCodesAfter(String date) throws SQLModuleException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<String[]> revokedCodes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .ACTIVE_FEDERATED_USER_CREATED_AFTER_CODE_QUERY)) {
            Date d = dateFormat.parse(date);
            prepStmt.setTimestamp(1, new Timestamp(d.getTime()));
            ResultSet resultSet = prepStmt.executeQuery();
            while (resultSet.next()) {
                String[] entry = {resultSet.getString(CODE_ID), resultSet.getString(AUTHZ_USER)};
                revokedCodes.add(entry);
            }
        } catch (SQLException | ParseException e) {
            throw new SQLModuleException(e);
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                     .REVOKE_ALL_FEDERATED_USER_CODES_AFTER_QUERY)) {
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }

        return revokedCodes;
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

    public Timestamp getFirstIssuedTime() throws SQLModuleException {

        Timestamp timestampToken = getFirstTokenIssuedTime();
        Timestamp timestampCode = getFirstCodeIssuedTime();
        if (timestampCode.after(timestampToken)) {
            return timestampToken;
        }
        return null;
    }

    private Timestamp getFirstTokenIssuedTime() throws SQLModuleException {

        Timestamp timestamp;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                    .FIRST_ACTIVE_TOKEN_ISSUED_TIME_QUERY);
            ResultSet resultSet = prepStmt.executeQuery();
            timestamp = resultSet.getTimestamp(1);
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
        return timestamp;
    }

    private Timestamp getFirstCodeIssuedTime() throws SQLModuleException {

        Timestamp timestamp;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement prepStmt = connection.prepareStatement(DAOConstants
                    .FIRST_ACTIVE_CODE_ISSUED_TIME_QUERY);
            ResultSet resultSet = prepStmt.executeQuery();
            timestamp = resultSet.getTimestamp(1);
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
        return timestamp;
    }

    private String processQueryContainingList(int numOfParams, String query, String placeHolder) {

        return query.replace(placeHolder, StringUtils.repeat("?", ",", numOfParams));
    }
}
