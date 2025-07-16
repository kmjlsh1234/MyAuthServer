package com.example.my_auth_server.config.security;

import com.example.my_auth_server.user.constants.LoginType;
import com.example.my_auth_server.user.model.LoginAddInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@Setter
public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final LoginAddInfo loginAddInfo;

    public CustomAuthenticationToken(Object principal, Object credentials, LoginAddInfo loginAddInfo) {
        super(principal, credentials);
        this.loginAddInfo = loginAddInfo;
        super.setAuthenticated(false);
    }
}
