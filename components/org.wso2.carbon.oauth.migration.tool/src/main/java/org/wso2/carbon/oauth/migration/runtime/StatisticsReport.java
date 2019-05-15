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
package org.wso2.carbon.oauth.migration.runtime;

import com.jakewharton.fliptables.FlipTable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.oauth.migration.sql.dao.FederatedUserMgtDAO;
import org.wso2.carbon.oauth.migration.sql.exception.SQLModuleException;
import org.wso2.carbon.oauth.migration.tool.OAuthTokenMigrator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StatisticsReport {

    private static final Logger log = LoggerFactory.getLogger(StatisticsReport.class);

    public boolean generate() throws OAuthMigrationExecutionException {

        String vulnerabilityStatus = "Vulnerability Status : Negative";
        List<Long> tokenExpirationTime;
        boolean isVulnerable = false;
        String[][] data;

        try {
            FederatedUserMgtDAO federatedUserMgtDAO = new FederatedUserMgtDAO();
            tokenExpirationTime = federatedUserMgtDAO.getTokenExpirationTime();
        } catch (SQLModuleException e) {
            throw new OAuthMigrationExecutionException("Could not get active tokens from database", e);
        }

        if (tokenExpirationTime.size() > 0) {
            if (log.isDebugEnabled()) {
                log.debug(MessageFormat.format("Fetched {0} active token(s) that belong to federated users",
                        tokenExpirationTime.size()));
            }
            vulnerabilityStatus = "Vulnerability Status : Positive";
            isVulnerable = true;
        }

        String[] headers = { vulnerabilityStatus };
        if (isVulnerable) {
            Long[] partitionedData = partitionDataset(tokenExpirationTime);
            data = new String[][]{{MessageFormat.format(readReportDescription(), partitionedData[0], partitionedData[1],
                    partitionedData[2], partitionedData[3], partitionedData[4], partitionedData[5], Collections
                            .max(tokenExpirationTime))}};
        } else {
            data = new String[][]{{"System is not affected by the vulnerability. No further actions required."}};
        }

        System.out.println(FlipTable.of(headers, data));

        return isVulnerable;
    }

    private String readReportDescription() {


        InputStream inputStream = OAuthTokenMigrator.class.getClassLoader()
                .getResourceAsStream("report_description.txt");
        String reportDescription = null;
        try {
            reportDescription = IOUtils.toString(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Report description not found");
        }
        return reportDescription;
    }

    private Long[] partitionDataset(List<Long> dataset) {

        Long[] partitionedData = {0L,0L,0L,0L,0L,0L};
        for(long duration : dataset) {
            if (duration > 365) {
                partitionedData[5]++;
            } else if (duration > 30) {
                partitionedData[4]++;
            } else if (duration > 7) {
                partitionedData[3]++;
            } else if (duration > 2) {
                partitionedData[2]++;
            } else if (duration > 1) {
                partitionedData[1]++;
            } else {
                partitionedData[0]++;
            }
        }
        return partitionedData;
    }
}
