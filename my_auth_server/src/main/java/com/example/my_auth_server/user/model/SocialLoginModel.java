package com.example.my_auth_server.user.model;

import com.example.my_auth_server.user.constants.ProviderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SocialLoginModel {
    private ProviderType providerType;
    private String idToken;
}
