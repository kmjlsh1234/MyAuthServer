package com.example.my_auth_server.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.example.my_auth_server.config.error.ErrorCode;
import com.example.my_auth_server.config.error.ResponseConstants;
import com.example.my_auth_server.config.security.constants.JwtProperties;
import com.example.my_auth_server.user.constants.LoginType;
import com.example.my_auth_server.user.model.LoginAddInfo;
import com.example.my_auth_server.user.service.JwtAuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.DatatypeConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtAuthenticationService jwtAuthenticationService;
    private final JWTVerifier jwtVerifier;
    private final ArrayList<String> excludeURL;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtAuthenticationService jwtAuthenticationService, String secret) {
        super(authenticationManager);
        this.jwtAuthenticationService = jwtAuthenticationService;
        jwtVerifier = JWT.require(Algorithm.HMAC512(secret.getBytes())).build();
        excludeURL = new ArrayList<>();
        excludeURL.add("/auth/token/refresh");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        //권한 HEADER 취득
        String header = request.getHeader(JwtProperties.HEADER_AUTH);
        String requestUrl = request.getRequestURI();

        if(header == null || excludeURL.contains(requestUrl)) {
            chain.doFilter(request, response);
            return;
        }

        Authentication authentication = getAuthentication(request, response);

        if(authentication == null) {
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);

    }

    private Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String token = request.getHeader(JwtProperties.HEADER_AUTH);

        DecodedJWT decodedJWT;

        try{
            decodedJWT = jwtVerifier.verify(token);
        } catch(TokenExpiredException e){
            errorResponse(response, ErrorCode.JWT_TOKEN_EXPIRATION);
            return null;
        } catch (SignatureVerificationException | JWTDecodeException | InvalidClaimException e) {
            errorResponse(response, ErrorCode.INVALID_AUTH_TOKEN);
            return null;
        } catch (JWTVerificationException e) {
            errorResponse(response, ErrorCode.JWT_TOKEN_AUTH_ERROR);
            return null;
        }

        String converted = decodedJWT.getSubject();
        String issueNo = decodedJWT.getId();
        String loginType = decodedJWT.getClaim(JwtProperties.CLAIM_LOGIN_TYPE).asString();

        ErrorCode errorCode = jwtAuthenticationService.checkBlackListToken(issueNo);
        if(errorCode != null){
            errorResponse(response, errorCode);
            return null;
        }

        String subject = new String(DatatypeConverter.parseHexBinary(converted));
        String[] subArray = subject.split(JwtProperties.SPLITTER);

        if(subArray[0] != null){ //loginId
            long userId = Long.parseLong(subArray[1]);
            String email = subArray[0];
            errorCode = jwtAuthenticationService.checkBlackListUser(userId);
            if(errorCode != null){
                errorResponse(response, errorCode);

                
            }
            LoginAddInfo loginAddInfo = new LoginAddInfo();
            loginAddInfo.setLoginType(LoginType.getLoginTypeAsType(loginType));

            UserPrincipal principal = UserPrincipal.builder()
                    .userId(userId)
                    .email(email)
                    .loginAddInfo(loginAddInfo)
                    .build();
            return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        }
        return null;
    }

    private void errorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put(ResponseConstants.ERROR_CODE, errorCode.getCode());
        resultMap.put(ResponseConstants.ERROR_MESSAGE, errorCode.getMessage());
        resultMap.put(ResponseConstants.ERROR_TIMESTAMP, ResponseConstants.DATE_FORMAT.format(new Date()));

        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = response.getWriter();
        out.print(mapper.writeValueAsString(resultMap));
        out.flush();

    }

}
