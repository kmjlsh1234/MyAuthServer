package com.example.my_auth_server.config.error.exception;

import com.example.my_auth_server.config.error.ErrorCode;
import lombok.Getter;

@Getter
public class RestException extends RuntimeException{
    private ErrorCode errorCode;

    public RestException(){
        super();
    }

    public RestException(final String message) {
        super(message);
    }

    public RestException (ErrorCode errorCode ){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
