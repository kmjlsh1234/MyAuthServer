package com.example.my_auth_server.user.vo;

import com.example.my_auth_server.user.constants.LoginType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserSimpleVo {
    private long userId;
    private String loginId;
    private String email;
    private LoginType loginType;
}
