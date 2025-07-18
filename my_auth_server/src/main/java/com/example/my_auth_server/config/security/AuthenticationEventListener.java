package com.example.my_auth_server.config.security;

import com.example.my_auth_server.user.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private final LoginAttemptService loginAttemptService;

    @EventListener
    public void authSuccessEventListener(AuthenticationSuccessEvent event) {
        UserPrincipal principal = (UserPrincipal) event.getAuthentication().getPrincipal();
        //로그인 시도 횟수 초기화
        log.info("Authentication Success Event : {}", principal.getEmail());
    }

    @EventListener
    public void authFailureEventListener(AuthenticationFailureBadCredentialsEvent event) {
        UserPrincipal principal = (UserPrincipal) event.getAuthentication().getPrincipal();
        //로그인 시도 휫수 추가
        log.info("Authentication Failure Event : {}", principal.getEmail());
    }
}
