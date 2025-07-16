package com.example.my_auth_server.user.model;

import com.example.my_auth_server.user.constants.LoginType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginAddInfo {
    private LoginType loginType;
}
