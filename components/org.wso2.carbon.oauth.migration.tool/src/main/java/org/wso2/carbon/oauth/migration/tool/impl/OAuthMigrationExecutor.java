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
package org.wso2.carbon.oauth.migration.tool.impl;

import org.apache.commons.io.IOUtils;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oauth.migration.common.logger.OAuthMigrationLogger;
import org.wso2.carbon.oauth.migration.common.util.CommonConstants;
import org.wso2.carbon.oauth.migration.config.SystemConfig;
import org.wso2.carbon.oauth.migration.runtime.OAuthMigrationExecutionException;
import org.wso2.carbon.oauth.migration.runtime.StatisticsReport;
import org.wso2.carbon.oauth.migration.sql.dao.FederatedUserMgtDAO;
import org.wso2.carbon.oauth.migration.sql.exception.SQLModuleException;
import org.wso2.carbon.oauth.migration.tool.Executor;
import org.wso2.carbon.oauth.migration.tool.OAuthTokenMigrator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.fusesource.jansi.Ansi.ansi;

public class OAuthMigrationExecutor implements Executor {

    private static final Logger log = LoggerFactory.getLogger(OAuthMigrationExecutor.class);
    private SystemConfig systemConfig;

    public OAuthMigrationExecutor() {
    }

    @Override
    public void setSystemConfig(SystemConfig systemConfig) {

        this.systemConfig = systemConfig;
    }

    @Override
    public void execute() throws OAuthMigrationExecutionException {

        generateOptions();

    }

    private static void generateOptions() throws OAuthMigrationExecutionException {

        TextIO textIO = TextIoFactory.getTextIO();
        ansi().eraseScreen();

        String data = readIssueDescription();
        System.out.println(data);

        int option = textIO.newIntInputReader().read("Please select your preference");

        switch(option) {
            case 1: initializeVulnerabilityScanner();
            case 2: cancel();
        }
    }

    private static String readIssueDescription() {

        InputStream inputStream = OAuthTokenMigrator.class.getClassLoader().getResourceAsStream("issue_description.txt");
        String issueDescription = null;
        try {
            issueDescription = IOUtils.toString(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Issue description not found");
        }
        return issueDescription;
    }

    private static void initializeVulnerabilityScanner() throws OAuthMigrationExecutionException {

        TextIO textIO = TextIoFactory.getTextIO();
        System.out.println("\nInitializing vulnerability scanner....\n");
        StatisticsReport statisticsReport = new StatisticsReport();
        boolean isVulnerable = statisticsReport.generate();

        if (isVulnerable) {
            int option = textIO.newIntInputReader().read("Please select your preference");
            if (option == 1) {
                try {
                    FederatedUserMgtDAO federatedUserMgtDAO = new FederatedUserMgtDAO();
                    OAuthMigrationLogger.writeToAuditLog(federatedUserMgtDAO.revokeAllTokens(), CommonConstants
                            .AUDIT_LOG_TYPE_TOKEN);
                    if (log.isDebugEnabled()) {
                        log.debug("Revoked all access tokens issued to affected federated users");
                    }
                } catch (SQLModuleException e) {
                    throw new OAuthMigrationExecutionException("Could not revoke tokens issued to affected users");
                }
            } else if(option == 2) {
                try {
                    FederatedUserMgtDAO federatedUserMgtDAO = new FederatedUserMgtDAO();
                    OAuthMigrationLogger.writeToAuditLog(federatedUserMgtDAO.revokeAllAuthorizationCodes(),
                            CommonConstants.AUDIT_LOG_TYPE_CODE);
                    if (log.isDebugEnabled()) {
                        log.debug("Revoked all authorization codes issued to affected federated users");
                    }
                } catch (SQLModuleException e) {
                    throw new OAuthMigrationExecutionException("Could not revoke authorization codes issued to " +
                            "affected users");
                }
            } else if(option == 3) {
                try {
                    FederatedUserMgtDAO federatedUserMgtDAO = new FederatedUserMgtDAO();
                    OAuthMigrationLogger.writeToAuditLog(federatedUserMgtDAO.revokeAllTokens(), CommonConstants
                            .AUDIT_LOG_TYPE_TOKEN);
                    OAuthMigrationLogger.writeToAuditLog(federatedUserMgtDAO.revokeAllAuthorizationCodes(),
                            CommonConstants.AUDIT_LOG_TYPE_CODE);
                    if (log.isDebugEnabled()) {
                        log.debug("Revoked all access tokens & authorization codes issued to affected federated users");
                    }
                } catch (SQLModuleException e) {
                    throw new OAuthMigrationExecutionException("Could not revoke authorization codes and access" +
                            " tokens issued to affected users");
                }
            } else {
                cancel();
            }

            System.out.println("\nSuccessfully tokens/authorization code issued to affected federated users\n");
        }
    }

    private static void cancel() {

        System.exit(0);
    }
}
