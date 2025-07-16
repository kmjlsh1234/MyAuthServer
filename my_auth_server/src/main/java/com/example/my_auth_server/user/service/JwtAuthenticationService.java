package com.example.my_auth_server.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.my_auth_server.config.error.ErrorCode;
import com.example.my_auth_server.config.error.exception.RestException;
import com.example.my_auth_server.config.security.constants.JwtProperties;
import com.example.my_auth_server.user.constants.LoginType;
import com.example.my_auth_server.user.model.JwtRecord;
import com.example.my_auth_server.user.model.RefreshToken;
import com.example.my_auth_server.user.repository.JwtRecordRepository;
import com.example.my_auth_server.user.repository.RefreshTokenRepository;
import com.example.my_auth_server.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.xml.bind.DatatypeConverter;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Slf4j
@Service
public class JwtAuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtRecordRepository jwtRecordRepository;
    private final JWTVerifier jwtVerifier;

    private final String secret;

    public JwtAuthenticationService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtRecordRepository jwtRecordRepository, @Value("${jwt.secret}") String secret) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtRecordRepository = jwtRecordRepository;
        this.secret = secret;
        jwtVerifier = JWT.require(HMAC512(secret.getBytes())).build();
    }

    // 새로운 리프레시 토큰(Refresh Token)을 생성해서 데이터베이스에 저장하는 메서드
    public RefreshToken issueRefreshToken(long userId) {
        String refreshTokenString = UUID.randomUUID().toString();
        LocalDateTime expireDateTime = LocalDateTime.now().plusDays(JwtProperties.REFRESH_TOKEN_EXPIRATION_DATE);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setRefreshToken(refreshTokenString);
        refreshToken.setExpireDatetime(expireDateTime);

        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    //Refresh Token 발급
    public Map<String, String> refreshToken(String token, String refreshToken, String ip) {
        HashMap<String, String> resultMap = new HashMap<>();
        DecodedJWT decodedJWT = verify(token);
        String converted = decodedJWT.getSubject();

        if(converted == null) {
            throw new RestException(ErrorCode.JWT_TOKEN_AUTH_ERROR);
        }

        if(refreshToken == null) {
            throw new RestException(ErrorCode.JWT_TOKEN_AUTH_ERROR);
        }

        String subject = new String(DatatypeConverter.parseHexBinary(converted));
        String[] subArray = subject.split(JwtProperties.SPLITTER);
        long userId = Long.parseLong(subArray[1]);
        if (subArray[0] == null) {

            throw new RestException(ErrorCode.JWT_TOKEN_AUTH_ERROR);
        }

        //가장 최근에 발급된 refresh token을 가져옴
        RefreshToken latestRefreshToken = refreshTokenRepository.findByUserIdAndRefreshToken(userId, refreshToken)
                .orElseThrow(() -> new RestException(ErrorCode.JWT_TOKEN_AUTH_ERROR));

        long refreshTokenId = latestRefreshToken.getId();
        LocalDateTime expireDateTime = latestRefreshToken.getExpireDatetime();
        LocalDateTime now = LocalDateTime.now();

        //만료일자가 현재보다 이전인지 검사(refresh토큰이 만료됨)
        if(expireDateTime.isBefore(now)) {
            throw new  RestException(ErrorCode.JWT_TOKEN_AUTH_ERROR);
        }

        if(expireDateTime.isBefore(now.plusDays(JwtProperties.REFRESH_TOKEN_NEED_REISSUE))) {
            RefreshToken newRefreshToken = issueRefreshToken(userId);
            refreshTokenId = newRefreshToken.getId();
            resultMap.put(JwtProperties.RESULT_MAP_REFRESH, newRefreshToken.getRefreshToken());
            refreshTokenRepository.delete(latestRefreshToken);
        }

        String email = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RestException(ErrorCode.NOT_EXIST_USER)).getEmail();
        LoginType loginType = LoginType.EMAIL;

        String authToken = createJwtToken(email, userId, ip, refreshTokenId, loginType);
        resultMap.put(JwtProperties.RESULT_MAP_AUTH, authToken);
        return resultMap;
    }

    private DecodedJWT verify(String token) {
        DecodedJWT decodedJWT;
        try{
            decodedJWT = jwtVerifier.verify(token);
        } catch (JWTVerificationException e){
            throw new RestException(ErrorCode.JWT_TOKEN_AUTH_ERROR);
        }
        return decodedJWT;

    }

    //JWT토큰 생성
    public String createJwtToken(String loginId, long userId, String ip, long refreshTokenId, LoginType loginType) {
        String subject = loginId + JwtProperties.SPLITTER + userId;
        String converted = DatatypeConverter.printHexBinary(subject.getBytes());
        LocalDateTime expireDate = LocalDateTime.now().plusSeconds(JwtProperties.EXPIRATION_TIME_3DAY_SECOND);

        JwtRecord jwtRecord = JwtRecord.builder()
                .userId(userId)
                .refreshTokenId(refreshTokenId)
                .ipAddress(ip)
                .expireDatetime(expireDate)
                .build();
        jwtRecordRepository.save(jwtRecord);
        jwtRecordRepository.flush();

        return JWT.create()
                .withSubject(converted)
                .withJWTId(String.valueOf(jwtRecord.getIssueNo()))
                .withClaim(JwtProperties.REFRESH_TOKEN_ID_KEY, refreshTokenId)
                .withClaim(JwtProperties.CLAIM_LOGIN_TYPE, loginType.toString())
                .withAudience(JwtProperties.AUDIENCE)
                .withExpiresAt(Timestamp.valueOf(expireDate))
                .sign(HMAC512(secret.getBytes()));
    }

    //블랙리스트에 토큰 넘버 있는지 조회
    public ErrorCode checkBlackListToken(String issueNo) {
        return null;
    }

    public ErrorCode checkBlackListUser(long userId) {
        return null;
    }
}
