package com.example.my_auth_server.config.security;

import com.example.my_auth_server.config.error.ErrorCode;
import com.example.my_auth_server.config.error.ResponseConstants;
import com.example.my_auth_server.social.service.GoogleVerificationService;
import com.example.my_auth_server.social.vo.SocialLoginVo;
import com.example.my_auth_server.social.vo.SocialUserInfo;
import com.example.my_auth_server.user.constants.ProviderType;
import com.example.my_auth_server.user.model.SocialLoginModel;
import com.example.my_auth_server.user.service.JwtAuthenticationService;
import com.example.my_auth_server.user.service.LoginSuccessAfterService;
import com.example.my_auth_server.util.WebUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

@Slf4j
public class SocialAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final GoogleVerificationService googleVerificationService;
    private final AuthenticationManager authenticationManager;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final LoginSuccessAfterService loginSuccessAfterService;
    private final WebUtil webUtil;

    public SocialAuthenticationFilter(GoogleVerificationService googleVerificationService, AuthenticationManager authenticationManager, JwtAuthenticationService jwtAuthenticationService, LoginSuccessAfterService loginSuccessAfterService, WebUtil webUtil) {
        this.googleVerificationService = googleVerificationService;
        this.authenticationManager = authenticationManager;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.loginSuccessAfterService = loginSuccessAfterService;
        this.webUtil = webUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        SocialLoginModel credential;
        try{
            credential = new ObjectMapper().readValue(request.getInputStream(), SocialLoginModel.class);
        } catch(IOException e){
            errorResponse(response, ErrorCode.SOCIAL_LOGIN_FAILURE_BAD_CREDENTIAL);
            return null;
        }

        ProviderType providerType = credential.getProviderType();
        String idToken = credential.getIdToken();

        SocialUserInfo socialUserInfo;
        try{
            socialUserInfo = googleVerificationService.getProviderInfo(providerType, idToken);
        } catch(Exception e){
            errorResponse(response, ErrorCode.SOCIAL_LOGIN_FAILURE_BAD_CREDENTIAL);
            return null;
        }

        String socialUniqueId = socialUserInfo.getSocialUniqueId();
        String email = socialUserInfo.getEmail();

        SocialLoginVo socialLoginVo =
    }

    private void okResultResponse(HttpServletResponse response, String result){
        response.setStatus(HttpStatus.OK.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try{
            PrintWriter out = response.getWriter();
            out.print(result);
            out.flush();
        } catch(Exception e){
            log.error(e.getMessage());
        }
    }

    private void errorResponse(HttpServletResponse response, ErrorCode errorCode){
        response.setStatus(errorCode.getStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put(ResponseConstants.ERROR_CODE, errorCode.getCode());
        resultMap.put(ResponseConstants.ERROR_MESSAGE, errorCode.getMessage());
        resultMap.put(ResponseConstants.ERROR_TIMESTAMP, ResponseConstants.DATE_FORMAT.format(new Date()));

        ObjectMapper mapper = new ObjectMapper();
        try {
            PrintWriter out = response.getWriter();
            out.print(mapper.writeValueAsString(resultMap));
            out.flush();
        } catch (Exception e) {
            log.error("error response fail");
        }
    }
}
