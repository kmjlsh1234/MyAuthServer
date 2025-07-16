package com.example.my_auth_server.user.model;

import com.example.my_auth_server.user.constants.LoginType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginViewModel {
    private String loginId;
    private String email;
    private String password;
    private String mobile;
    private LoginType loginType;
}
