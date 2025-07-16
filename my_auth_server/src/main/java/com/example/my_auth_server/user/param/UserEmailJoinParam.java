package com.example.my_auth_server.user.param;

import lombok.Getter;

@Getter
public class UserEmailJoinParam {
    private String email;
    private String password;
    private String mobile;
}
