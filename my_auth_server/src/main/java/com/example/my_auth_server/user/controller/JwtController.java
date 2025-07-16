package com.example.my_auth_server.user.controller;

import com.example.my_auth_server.config.security.constants.JwtProperties;
import com.example.my_auth_server.user.param.RefreshTokenParam;
import com.example.my_auth_server.user.service.JwtAuthenticationService;
import com.example.my_auth_server.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class JwtController {

    private final JwtAuthenticationService jwtAuthenticationService;
    private final WebUtil webUtil;

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader(name = "Authorization") String authToken,
                                          @RequestBody RefreshTokenParam refreshTokenParam,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        String ip = webUtil.getClientIp(request);
        Map<String, String> newTokens = jwtAuthenticationService.refreshToken(authToken, refreshTokenParam.getRefreshToken(), ip);
        response.addHeader(JwtProperties.HEADER_AUTH, newTokens.get(JwtProperties.RESULT_MAP_AUTH));
        if(newTokens.containsKey(JwtProperties.RESULT_MAP_AUTH)){
            response.addHeader(JwtProperties.REFRESH_HEADER_STRING, newTokens.get(JwtProperties.RESULT_MAP_REFRESH));
        }
        return ResponseEntity.ok().build();
    }
}
