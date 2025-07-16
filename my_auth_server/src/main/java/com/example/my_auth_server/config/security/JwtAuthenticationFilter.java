package com.example.my_auth_server.config.security;

import com.example.my_auth_server.config.error.ErrorCode;
import com.example.my_auth_server.config.error.ResponseConstants;
import com.example.my_auth_server.config.error.exception.RestException;
import com.example.my_auth_server.config.security.constants.JwtProperties;
import com.example.my_auth_server.user.constants.LoginType;
import com.example.my_auth_server.user.model.LoginAddInfo;
import com.example.my_auth_server.user.model.LoginViewModel;
import com.example.my_auth_server.user.model.RefreshToken;
import com.example.my_auth_server.user.service.JwtAuthenticationService;
import com.example.my_auth_server.user.service.LoginAttemptService;
import com.example.my_auth_server.user.service.LoginSuccessAfterService;
import com.example.my_auth_server.user.vo.UserSimpleVo;
import com.example.my_auth_server.util.WebUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final LoginSuccessAfterService loginSuccessAfterService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final WebUtil webUtil;


    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, LoginAttemptService loginAttemptService, LoginSuccessAfterService loginSuccessAfterService, JwtAuthenticationService jwtAuthenticationService, WebUtil webUtil) {
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
        this.loginSuccessAfterService = loginSuccessAfterService;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.webUtil = webUtil;
        this.setFilterProcessesUrl("/auth/login");
    }


    /**
     * 로그인 시도
     *
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        LoginViewModel loginViewModel;

        try{
            loginViewModel = new ObjectMapper().readValue(request.getInputStream(), LoginViewModel.class);
        } catch (Exception e){
            errorResponse(response, ErrorCode.LOGIN_FAILURE_NO_CREDENTIAL, 0);
            return null;
        }

        //필수 입력정보가 없음
        if(loginViewModel == null){
            errorResponse(response, ErrorCode.LOGIN_FAILURE_REQUIRED_PARAMETER, 0);
            return null;
        }

        LoginType loginType = loginViewModel.getLoginType();

        String id = switch (loginType){
            case GUEST,ID_PASS -> loginViewModel.getLoginId();
            case EMAIL -> loginViewModel.getEmail();
            case MOBILE -> loginViewModel.getMobile();
            default -> null;
        };

        if(StringUtils.isEmpty(id)){
            errorResponse(response, ErrorCode.LOGIN_FAILURE_REQUIRED_PARAMETER, 0);
            return null;
        }

        LoginAddInfo loginAddInfo = new LoginAddInfo();
        loginAddInfo.setLoginType(loginType);

        CustomAuthenticationToken authenticationToken = new CustomAuthenticationToken(id, loginViewModel.getPassword(), loginAddInfo);
        Authentication authentication;
        try{
          authentication =  authenticationManager.authenticate(authenticationToken);
        } catch(Exception e){
            Object cause = e.getCause();
            if(cause instanceof RestException){
                ErrorCode errorCode = ((RestException)cause).getErrorCode();
                errorResponse(response, errorCode, 0);
            } else{
                if(e instanceof BadCredentialsException){
                    int count = loginAttemptService.getCount(loginViewModel.getLoginId());
                    errorResponse(response, ErrorCode.LOGIN_FAILURE_NO_CREDENTIAL, count);
                } else{
                    errorResponse(response, ErrorCode.LOGIN_FAILURE_BAD_CREDENTIAL, 0);
                }
            }
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

    private void errorResponse(HttpServletResponse response, ErrorCode errorCode, int count){
        response.setStatus(errorCode.getStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put(ResponseConstants.ERROR_CODE, errorCode.getCode());
        resultMap.put(ResponseConstants.ERROR_MESSAGE, errorCode.getMessage());
        resultMap.put(ResponseConstants.ERROR_TIMESTAMP, ResponseConstants.DATE_FORMAT.format(new Date()));

        if(count > 0){
            resultMap.put(ResponseConstants.ERROR_COUNT, count);
        }
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
