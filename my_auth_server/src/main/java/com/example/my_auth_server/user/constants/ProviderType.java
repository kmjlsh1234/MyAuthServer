package com.example.my_auth_server.user.constants;

import lombok.Getter;

@Getter
public enum ProviderType {
    GOOGLE("GOOGLE"),
    NAVER("NAVER"),
    KAKAO("KAKAO"),
    ;
    private String value;
    ProviderType(String value) {
        this.value = value;
    }
}
