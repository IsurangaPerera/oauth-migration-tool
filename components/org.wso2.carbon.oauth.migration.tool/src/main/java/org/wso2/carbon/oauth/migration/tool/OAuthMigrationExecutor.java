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

import org.apache.commons.collections.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oauth.migration.common.runtime.ModuleException;
import org.wso2.carbon.oauth.migration.config.SystemConfig;
import org.wso2.carbon.oauth.migration.log.scanner.LogScanner;
import org.wso2.carbon.oauth.migration.runtime.OAuthMigrationExecutionException;
import org.wso2.carbon.oauth.migration.sql.dao.FederatedUserMgtDAO;

import java.io.Console;
import java.text.MessageFormat;
import java.util.List;

public class OAuthMigrationExecutor {

    private static final Logger log = LoggerFactory.getLogger(OAuthMigrationExecutor.class);

    private SystemConfig systemConfig;

    public OAuthMigrationExecutor(SystemConfig systemConfig) {

        this.systemConfig = systemConfig;
    }

    /**
     * Executes the engine.
     *
     * @throws OAuthMigrationExecutionException upon any error while executing the set of instructions.
     */
    public void execute() throws OAuthMigrationExecutionException {

        List<String> federatedUsersFromDB;
        List<String> federatedUsersFromLogs;
        LogScanner logScanner = systemConfig.getLogScanner();
        try {
            FederatedUserMgtDAO federatedUserMgtDAO = new FederatedUserMgtDAO();
            federatedUsersFromDB = federatedUserMgtDAO.getActiveFederatedUsers();
            log.info(MessageFormat.format("{0} federated user(s) with active tokens and " +
                    "authorization codes fetched from the DB", federatedUsersFromDB.size()));

            if (federatedUsersFromDB.size() > 0) {
                federatedUsersFromLogs = logScanner.processAuditLogs();
                log.info(MessageFormat.format("{0} federated user(s) with active tokens and " +
                        "authorization codes fetched from audit logs", federatedUsersFromLogs.size()));

                if (isAccessRevokable(federatedUsersFromLogs)) {
                    federatedUserMgtDAO.revokeTokensAndCodes(federatedUsersFromLogs);
                    federatedUsersFromDB = ListUtils.subtract(federatedUsersFromDB, federatedUsersFromLogs);
                }
            }
            federatedUserMgtDAO.migrateFederatedUsers();
        } catch (ModuleException e) {
            throw new OAuthMigrationExecutionException(e.getMessage());
        }
    }

    private boolean isAccessRevokable(List<String> federatedUsersFromLogs) throws OAuthMigrationExecutionException {

        if (federatedUsersFromLogs.size() > 0) {
            Console console = System.console();
            if (console == null) {
                throw new OAuthMigrationExecutionException("Unable to fetch the console");
            }
            console.printf(MessageFormat.format("{0} federated users found from audit logs who have multiple " +
                    "login entries. Do you want to revoke associated tokens and authorization codes?(yes/no) : ",
                    federatedUsersFromLogs.size()));
            String response = console.readLine().trim();
            return response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes");
        }
        return false;
    }
}