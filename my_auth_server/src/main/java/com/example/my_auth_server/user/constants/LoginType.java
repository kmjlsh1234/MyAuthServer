package com.example.my_auth_server.user.constants;

import lombok.Getter;

@Getter
public enum LoginType {
    EMAIL(0),
    MOBILE(1),
    SOCIAL(2),
    ID_PASS(3),
    GUEST(4),
    ;
    private int value;

    LoginType(int value) {
        this.value = value;
    }
    public static LoginType getLoginTypeAsType(String loginType) {
        return LoginType.valueOf(loginType.toUpperCase());
    }
}
