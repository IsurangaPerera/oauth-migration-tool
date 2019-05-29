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

public class DAOConstants {

    public static String TIME_CREATED = "TIME_CREATED";
    public static String VALIDITY_PERIOD = "VALIDITY_PERIOD";

    public static final String ACTIVE_FEDERATED_USERS_WITH_TOKEN_QUERY = "SELECT AUTHZ_USER FROM " +
            "IDN_OAUTH2_ACCESS_TOKEN WHERE USER_DOMAIN = \'FEDERATED\'";
    public static final String ACTIVE_FEDERATED_USERS_WITH_CODE_QUERY = "SELECT AUTHZ_USER FROM " +
            "IDN_OAUTH2_AUTHORIZATION_CODE WHERE USER_DOMAIN = \'FEDERATED\'";

    public static final String REVOKE_FEDERATED_USER_TOKENS_QUERY = "DELETE FROM IDN_OAUTH2_ACCESS_TOKEN WHERE " +
            "AUTHZ_USER IN (#) AND USER_DOMAIN = \'FEDERATED\'";
    public static final String REVOKE_FEDERATED_USER_CODES_QUERY = "DELETE FROM IDN_OAUTH2_AUTHORIZATION_CODE WHERE " +
            "AUTHZ_USER IN (#) AND USER_DOMAIN = \'FEDERATED\'";
    public static final String REVOKE_ALL_FEDERATED_USER_TOKENS_QUERY = "DELETE FROM IDN_OAUTH2_ACCESS_TOKEN WHERE " +
            "USER_DOMAIN = \'FEDERATED\'";
    public static final String REVOKE_ALL_FEDERATED_USER_CODES_QUERY = "DELETE FROM " +
            "IDN_OAUTH2_AUTHORIZATION_CODE WHERE USER_DOMAIN = \'FEDERATED\'";

    public static final String REVOKE_ALL_FEDERATED_USER_TOKENS_AFTER_QUERY = "DELETE FROM IDN_OAUTH2_ACCESS_TOKEN WHERE " +
            "USER_DOMAIN = \'FEDERATED\' AND TIME_CREATED > ?";
    public static final String REVOKE_ALL_FEDERATED_USER_CODES_AFTER_QUERY = "DELETE FROM " +
            "IDN_OAUTH2_AUTHORIZATION_CODE WHERE USER_DOMAIN = \'FEDERATED\' AND TIME_CREATED > ?";

    public static final String MIGRATE_TOKEN_QUERY = "UPDATE IDN_OAUTH2_ACCESS_TOKEN SET " +
            "IDN_OAUTH2_ACCESS_TOKEN.IDP_ID = (SELECT IDP_ID FROM IDN_AUTH_USER WHERE " +
            "IDN_OAUTH2_ACCESS_TOKEN.AUTHZ_USER =  IDN_AUTH_USER.USER_NAME AND " +
            "IDN_AUTH_USER.DOMAIN_NAME = \'FEDERATED\') WHERE IDN_OAUTH2_ACCESS_TOKEN.USER_DOMAIN = \'FEDERATED\'";
    public static final String MIGRATE_AUTHORIZATION_CODE_QUERY = "UPDATE IDN_OAUTH2_AUTHORIZATION_CODE SET " +
            "IDN_OAUTH2_AUTHORIZATION_CODE.IDP_ID = (SELECT IDP_ID FROM IDN_AUTH_USER WHERE " +
            "IDN_OAUTH2_AUTHORIZATION_CODE.AUTHZ_USER =  IDN_AUTH_USER.USER_NAME AND " +
            "IDN_AUTH_USER.DOMAIN_NAME = \'FEDERATED\') WHERE IDN_OAUTH2_AUTHORIZATION_CODE.USER_DOMAIN = \'FEDERATED\'";

    public static final String FIRST_ACTIVE_TOKEN_ISSUED_TIME_QUERY = "SELECT MIN(TIME_CREATED) as " +
            "FIRST_TOKEN_ISSUED_TIME FROM  IDN_OAUTH2_ACCESS_TOKEN";
    public static final String FIRST_ACTIVE_CODE_ISSUED_TIME_QUERY = "SELECT MIN(TIME_CREATED) as " +
            "FIRST_CODE_ISSUED_TIME FROM  IDN_OAUTH2_AUTHORIZATION_CODE";

    public static final String TOKEN_EXPIRATION_TIME_QUERY = "SELECT TIME_CREATED, VALIDITY_PERIOD " +
            "FROM IDN_OAUTH2_ACCESS_TOKEN";

    public static final String ACTIVE_FEDERATED_USER_ENTRY_TOKEN_QUERY = "SELECT TOKEN_ID, AUTHZ_USER FROM " +
            "IDN_OAUTH2_ACCESS_TOKEN WHERE USER_DOMAIN = \'FEDERATED\'";
    public static final String ACTIVE_FEDERATED_USER_ENTRY_CODE_QUERY = "SELECT CODE_ID, AUTHZ_USER FROM " +
            "IDN_OAUTH2_AUTHORIZATION_CODE WHERE USER_DOMAIN = \'FEDERATED\'";

    public static final String ACTIVE_FEDERATED_USER_CREATED_AFTER_TOKEN_QUERY = "SELECT TOKEN_ID, AUTHZ_USER FROM " +
            "IDN_OAUTH2_ACCESS_TOKEN WHERE USER_DOMAIN = \'FEDERATED\' AND TIME_CREATED > ?";
    public static final String ACTIVE_FEDERATED_USER_CREATED_AFTER_CODE_QUERY = "SELECT CODE_ID, AUTHZ_USER FROM " +
            "IDN_OAUTH2_AUTHORIZATION_CODE WHERE USER_DOMAIN = \'FEDERATED\' AND TIME_CREATED > ?";
}
