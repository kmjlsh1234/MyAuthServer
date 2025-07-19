package com.example.my_auth_server.config.security;

import com.example.my_auth_server.social.service.GoogleVerificationService;
import com.example.my_auth_server.user.service.JwtAuthenticationService;
import com.example.my_auth_server.user.service.LoginAttemptService;
import com.example.my_auth_server.user.service.LoginSuccessAfterService;
import com.example.my_auth_server.user.service.SocialUserAuthenticationService;
import com.example.my_auth_server.util.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final LoginAttemptService loginAttemptService;
    private final LoginSuccessAfterService loginSuccessAfterService;
    private final SocialUserAuthenticationService socialUserAuthenticationService;
    private final GoogleVerificationService googleVerificationService;
    private final Environment env;
    private final WebUtil webUtil;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${jwt.secret}")
    private String secret;

    public SecurityConfiguration(UserDetailsService userDetailsService, JwtAuthenticationService jwtAuthenticationService, LoginAttemptService loginAttemptService, LoginSuccessAfterService loginSuccessAfterService, SocialUserAuthenticationService socialUserAuthenticationService, GoogleVerificationService googleVerificationService, Environment env, WebUtil webUtil, @Autowired ApplicationEventPublisher applicationEventPublisher) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.loginAttemptService = loginAttemptService;
        this.loginSuccessAfterService = loginSuccessAfterService;
        this.socialUserAuthenticationService = socialUserAuthenticationService;
        this.googleVerificationService = googleVerificationService;
        this.env = env;
        this.webUtil = webUtil;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http, SocialUserAuthenticationService socialUserAuthenticationService) throws Exception {
        //스프링이 내부적으로 사용하는 AuthenticationManagerBuilder를 꺼내옴.
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        //로그인 성공/실패 같은 인증 이벤트 발생 시 이벤트를 스프링에 전달하도록 설정
        authenticationManagerBuilder.authenticationEventPublisher(defaultAuthenticationEventPublisher(applicationEventPublisher));

        //비밀번호같은 민감한 정보는 메모리에서 지워지게 설정, 인증 매니저를 빌드해서 적용
        http.authenticationManager(authenticationManagerBuilder.eraseCredentials(true).build());

        //직접 만든 인증로직(CustomUserDetailAuthenticationProvider)등록
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());
        http
                .csrf(CsrfConfigurer::disable)  //CSRF 보호 끄기(JWT는 세션 사용X)
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilter(new JwtAuthenticationFilter(authenticationManagerBuilder.getObject(), loginAttemptService, loginSuccessAfterService, jwtAuthenticationService, webUtil))
                .addFilter(new JwtAuthorizationFilter(authenticationManagerBuilder.getObject(), jwtAuthenticationService, secret))
                .addFilter(new SocialAuthenticationFilter(authenticationManagerBuilder.getObject(),googleVerificationService, jwtAuthenticationService, loginSuccessAfterService, socialUserAuthenticationService, webUtil))
                .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                        "/test/**", "/auth/join/**", "/auth/token/refresh"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                 .anyRequest().authenticated()
                )
                .exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public DefaultAuthenticationEventPublisher defaultAuthenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }

    @Bean
    public CustomUserDetailsAuthenticationProvider authenticationProvider() {
        CustomUserDetailsAuthenticationProvider authenticationProvider = new CustomUserDetailsAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userDetailsService);
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
