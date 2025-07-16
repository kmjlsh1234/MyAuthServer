package com.example.my_auth_server.main.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping("/test/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/auth/ping")
    public String authPing() {
        return "pong";
    }
}
