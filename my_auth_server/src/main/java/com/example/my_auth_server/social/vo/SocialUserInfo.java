package com.example.my_auth_server.social.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SocialUserInfo {
    private String socialUniqueId;
    private String email;
}
