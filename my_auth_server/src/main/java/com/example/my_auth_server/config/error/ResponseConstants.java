package com.example.my_auth_server.config.error;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ResponseConstants {
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_COUNT = "errorCount";
    public static final String ERROR_MESSAGE = "message";
    public static final String ERROR_TIMESTAMP = "timestamp";
    public static final String SOCIAL_VALIDATION_CODE = "validationCode";
    public static final String SOCIAL_AUTH_URL = "authUrl";
    public static final String SOCIAL_TX_ID = "txId";

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
}
