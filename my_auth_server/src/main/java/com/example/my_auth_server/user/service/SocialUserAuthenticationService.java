package com.example.my_auth_server.user.service;

import com.example.my_auth_server.config.error.ErrorCode;
import com.example.my_auth_server.config.error.exception.RestException;
import com.example.my_auth_server.social.vo.SocialLoginVo;
import com.example.my_auth_server.user.model.SocialLogin;
import com.example.my_auth_server.user.repository.SocialLoginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialUserAuthenticationService {

    private final SocialLoginRepository socialLoginRepository;

    public SocialLoginVo getSocialLogin(String socialId) {
        SocialLogin socialLogin = socialLoginRepository.findBySocialId(socialId)
                .orElse(null);

        if(socialLogin == null) {
            return null;
        }
        return SocialLoginVo.builder()
                .id(socialLogin.getId())
                .userId(socialLogin.getUserId())
                .socialId(socialId)
                .providerType(socialLogin.getProviderType())
                .build();
    }
}
