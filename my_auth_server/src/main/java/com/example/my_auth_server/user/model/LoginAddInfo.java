package com.example.my_auth_server.user.model;

import com.example.my_auth_server.user.constants.LoginType;
import com.example.my_auth_server.user.constants.ProviderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginAddInfo {
    private LoginType loginType;
    private ProviderType providerType;
}
