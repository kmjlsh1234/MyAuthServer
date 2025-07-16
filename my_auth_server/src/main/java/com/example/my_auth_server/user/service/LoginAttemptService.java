package com.example.my_auth_server.user.service;

import com.example.my_auth_server.config.redis.LoginAttempt;
import com.example.my_auth_server.config.redis.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;

    private final int MAX_ATTEMPT = 5;

    //Block 상태인지 확인
    public boolean isBlocked(String id) {
        LoginAttempt loginAttempt = loginAttemptRepository.findById(id).orElse(null);
        if (loginAttempt == null) {
            return false;
        }

        return loginAttempt.getCnt() >= MAX_ATTEMPT;
    }

    public int getCount(String id) {
        return loginAttemptRepository.findById(id)
                .map(LoginAttempt::getCnt)
                .orElse(0);
    }
}
