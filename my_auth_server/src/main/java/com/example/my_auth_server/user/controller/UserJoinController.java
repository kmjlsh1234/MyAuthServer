package com.example.my_auth_server.user.controller;

import com.example.my_auth_server.user.param.UserEmailJoinParam;
import com.example.my_auth_server.user.service.UserJoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserJoinController {

    private final UserJoinService userJoinService;

    @PostMapping("/join/test")
    public ResponseEntity<?> joinUserTest(@RequestBody UserEmailJoinParam userJoinParam) {
        userJoinService.joinUserTest(userJoinParam);
        return ResponseEntity.ok().build();
    }
}
