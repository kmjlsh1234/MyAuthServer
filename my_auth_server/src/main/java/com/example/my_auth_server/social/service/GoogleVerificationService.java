package com.example.my_auth_server.social.service;

import com.example.my_auth_server.config.error.ErrorCode;
import com.example.my_auth_server.config.error.exception.RestException;
import com.example.my_auth_server.social.constants.GoogleConstants;
import com.example.my_auth_server.social.vo.SocialUserInfo;
import com.example.my_auth_server.user.constants.ProviderType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Slf4j
@Service
public class GoogleVerificationService {

    private final GoogleIdTokenVerifier verifier;

    public GoogleVerificationService() {
        this.verifier = new GoogleIdTokenVerifier
                .Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(GoogleConstants.AUDIENCE))
                .build();
    }

    public SocialUserInfo getProviderInfo(ProviderType providerType, String idToken) {
        return switch(providerType){
            case NAVER -> null;
            case KAKAO -> null;
            case GOOGLE -> getGoogleProviderInfo(idToken);
        };
    }

    private SocialUserInfo getGoogleProviderInfo(String credential) {
        GoogleIdToken idToken;
        try{
            idToken = verifier.verify(credential);
        } catch (GeneralSecurityException | IOException e){
            throw new RestException(ErrorCode.JWT_TOKEN_AUTH_ERROR);
        }

        if(idToken == null){
            throw new RestException(ErrorCode.JWT_TOKEN_AUTH_ERROR);
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String socialId = payload.getSubject();
        String email = payload.getEmail();
        return SocialUserInfo.builder()
                .email(email)
                .socialUniqueId(socialId)
                .build();
    }

}
