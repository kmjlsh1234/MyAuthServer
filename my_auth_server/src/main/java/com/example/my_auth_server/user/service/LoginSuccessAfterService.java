package com.example.my_auth_server.user.service;

import com.example.my_auth_server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginSuccessAfterService {

    private final UserRepository userRepository;


}
