package org.wso2.carbon.oauth.migration.sql.dao;

public class DAOConstants {

    public static final String ACTIVE_FEDERATED_USERS_WITH_TOKEN_QUERY = "SELECT AUTHZ_USER FROM " +
            "IDN_OAUTH2_ACCESS_TOKEN WHERE USER_DOMAIN = \'FEDERATED\'";
    public static final String ACTIVE_FEDERATED_USERS_WITH_CODE_QUERY = "SELECT AUTHZ_USER FROM " +
            "IDN_OAUTH2_AUTHORIZATION_CODE WHERE USER_DOMAIN = \'FEDERATED\'";
    public static final String REVOKE_FEDERATED_USER_TOKENS_QUERY = "DELETE FROM IDN_OAUTH2_ACCESS_TOKEN WHERE " +
            "AUTHZ_USER IN ? AND USER_DOMAIN = \'FEDERATED\'";
    public static final String REVOKE_FEDERATED_USER_CODES_QUERY = "DELETE FROM IDN_OAUTH2_AUTHORIZATION_CODE WHERE " +
            "AUTHZ_USER IN ? AND USER_DOMAIN = \'FEDERATED\'";
    public static final String MIGRATE_TOKEN_QUERY = "UPDATE IDN_OAUTH2_ACCESS_TOKEN SET " +
            "IDN_OAUTH2_ACCESS_TOKEN.IDP_ID = (SELECT IDP_ID FROM IDN_AUTH_USER WHERE " +
            "IDN_OAUTH2_ACCESS_TOKEN.AUTHZ_USER =  IDN_AUTH_USER.USER_NAME AND " +
            "IDN_AUTH_USER.DOMAIN_NAME = \'FEDERATED\') WHERE IDN_OAUTH2_ACCESS_TOKEN.USER_DOMAIN = \'FEDERATED\'";
    public static final String MIGRATE_AUTHORIZATION_CODE_QUERY = "UPDATE IDN_OAUTH2_AUTHORIZATION_CODE SET " +
            "IDN_OAUTH2_AUTHORIZATION_CODE.IDP_ID = (SELECT IDP_ID FROM IDN_AUTH_USER WHERE " +
            "IDN_OAUTH2_AUTHORIZATION_CODE.AUTHZ_USER =  IDN_AUTH_USER.USER_NAME AND " +
            "IDN_AUTH_USER.DOMAIN_NAME = \'FEDERATED\') WHERE IDN_OAUTH2_AUTHORIZATION_CODE.USER_DOMAIN = \'FEDERATED\'";
}
