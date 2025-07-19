package com.example.my_auth_server.config.security;

import com.example.my_auth_server.config.error.ErrorCode;
import com.example.my_auth_server.config.error.ResponseConstants;
import com.example.my_auth_server.config.error.exception.RestException;
import com.example.my_auth_server.config.security.constants.JwtProperties;
import com.example.my_auth_server.social.service.GoogleVerificationService;
import com.example.my_auth_server.social.vo.SocialLoginVo;
import com.example.my_auth_server.social.vo.SocialUserInfo;
import com.example.my_auth_server.user.constants.LoginType;
import com.example.my_auth_server.user.constants.ProviderType;
import com.example.my_auth_server.user.model.LoginAddInfo;
import com.example.my_auth_server.user.model.RefreshToken;
import com.example.my_auth_server.user.model.SocialLoginModel;
import com.example.my_auth_server.user.service.JwtAuthenticationService;
import com.example.my_auth_server.user.service.LoginSuccessAfterService;
import com.example.my_auth_server.user.service.SocialUserAuthenticationService;
import com.example.my_auth_server.user.vo.UserSimpleVo;
import com.example.my_auth_server.util.WebUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

@Slf4j
public class SocialAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final GoogleVerificationService googleVerificationService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final LoginSuccessAfterService loginSuccessAfterService;
    private final SocialUserAuthenticationService socialUserAuthenticationService;
    private final WebUtil webUtil;

    public SocialAuthenticationFilter(AuthenticationManager authenticationManager,GoogleVerificationService googleVerificationService,  JwtAuthenticationService jwtAuthenticationService, LoginSuccessAfterService loginSuccessAfterService, SocialUserAuthenticationService socialUserAuthenticationService, WebUtil webUtil) {
        this.googleVerificationService = googleVerificationService;
        this.authenticationManager = authenticationManager;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.loginSuccessAfterService = loginSuccessAfterService;
        this.socialUserAuthenticationService = socialUserAuthenticationService;
        this.webUtil = webUtil;
        this.setFilterProcessesUrl("/auth/login/social");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
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

        /* 소셜 로그인 정보 확인 */
        SocialLoginVo socialLoginVo = socialUserAuthenticationService.getSocialLogin(socialUniqueId);

        //가입 안되어있으면 본인인증 + 회원가입 유도
        if(socialLoginVo == null){
            try {
                ObjectMapper mapper = new ObjectMapper();
                //String result = mapper.writeValueAsString(askToSignUpResult(providerType, socialUniqueId, socialEmail));
                //okResultResponse(response, result);
                return null;
            } catch (RestException e) {
                errorResponse(response, e.getErrorCode());
                return null;
            }
        }

        LoginAddInfo loginAddInfo = new LoginAddInfo();
        loginAddInfo.setLoginType(LoginType.SOCIAL);
        loginAddInfo.setProviderType(providerType);

        CustomAuthenticationToken authenticationToken = new CustomAuthenticationToken(email, null, loginAddInfo);
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch(Exception e){
            errorResponse(response, ErrorCode.SOCIAL_LOGIN_FAILURE_BAD_CREDENTIAL);
            return null;
        }

        return authentication;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        UserPrincipal principal = (UserPrincipal) authResult.getPrincipal();

        //필요에 따라 loginServiceAfterService 메서드 호출하기

        String realIp = webUtil.getClientIp(request);

        //RefreshToken 생성
        RefreshToken refreshToken = jwtAuthenticationService.issueRefreshToken(principal.getUserId());

        //JWTToken 생성 후 Response Header에 추가
        String jwtToken = jwtAuthenticationService.createJwtToken(principal.getLoginId(), principal.getUserId(), realIp, refreshToken.getId(), principal.getLoginType());
        response.addHeader(JwtProperties.HEADER_AUTH, jwtToken);

        //refresh토큰 추가
        if(refreshToken.getRefreshToken() != null){
            response.addHeader(JwtProperties.REFRESH_HEADER_STRING, refreshToken.getRefreshToken());
        }

        try{
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            String result = mapper.writeValueAsString(UserSimpleVo.builder()
                    .userId(principal.getUserId())
                    .loginId(principal.getLoginId())
                    .email(principal.getEmail())
                    .loginType(principal.getLoginType())
                    .build());
            ;okResultResponse(response, result);
        } catch (Exception e){
            log.error(e.getMessage());
        }
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
