package com.example.my_auth_server.user.service;

import com.example.my_auth_server.user.constants.LoginType;
import com.example.my_auth_server.user.model.Users;
import com.example.my_auth_server.user.param.UserEmailJoinParam;
import com.example.my_auth_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserJoinService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void joinUserTest(UserEmailJoinParam userJoinParam) {
        userRepository.save(Users.builder()
                .email(userJoinParam.getEmail())
                .password(passwordEncoder.encode(userJoinParam.getPassword()))
                .mobile(userJoinParam.getMobile())
                .loginType(LoginType.EMAIL)
                .build());
    }
}
