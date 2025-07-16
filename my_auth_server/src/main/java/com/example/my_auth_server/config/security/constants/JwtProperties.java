package com.example.my_auth_server.config.security.constants;

public class JwtProperties {
    public static final String SPLITTER = ",";
    public static final int REFRESH_TOKEN_EXPIRATION_DATE = 30;
    public static final int EXPIRATION_TIME_3DAY_SECOND = 259_200;
    public static final String AUDIENCE = "net.lodgames";
    public static final String CLAIM_LOGIN_TYPE = "login_type";
    public static final String CLAIM_OS = "os";
    public static final String REFRESH_TOKEN_ID_KEY = "refresh";
    public static final String RESULT_MAP_REFRESH = "refresh";
    public static final String HEADER_AUTH = "Authorization";
    public static final String REFRESH_HEADER_STRING = "ReAuthentication";
    public static final int REFRESH_TOKEN_NEED_REISSUE = 4;
    public static final String RESULT_MAP_AUTH = "auth";// 4 days

}
