package com.example.my_auth_server.social.vo;

import com.example.my_auth_server.user.constants.ProviderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SocialLoginVo {
    private long id;
    private long identityVerificationId;
    private long userId;
    private String socialId;
    private ProviderType providerType;
}
