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

package org.wso2.carbon.oauth.migration.log.scanner.factory;

import org.wso2.carbon.oauth.migration.common.util.CommonConstants;
import org.wso2.carbon.oauth.migration.log.scanner.LogScanner;
import org.wso2.carbon.oauth.migration.log.scanner.impl.BasicLogScanner;
import org.wso2.carbon.oauth.migration.log.scanner.impl.OSBasedLogScanner;

public class LogScannerFactory {

    private LogScannerFactory() {}

    public static LogScanner getScanner(String scanner) {

        switch(scanner) {
            case CommonConstants.BASIC_LOG_SCANNER :
                return getBasicLogScanner();
            case CommonConstants.OS_BASED_LOG_SCANNER :
                return getOSBasedLogScanner();
            default :
                return getBasicLogScanner();
        }
    }

    public static BasicLogScanner getBasicLogScanner() {

        return new BasicLogScanner();
    }

    public static OSBasedLogScanner getOSBasedLogScanner() {

        return new OSBasedLogScanner();
    }
}
