package com.example.my_auth_server.config.security;

import com.example.my_auth_server.user.constants.LoginType;
import com.example.my_auth_server.user.model.LoginAddInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
public class UserPrincipal implements UserDetails {

    private final long userId;
    private final String loginId;
    private final LoginType loginType;
    private final String password;
    private String email;
    private final String mobile;
    private final String nickname;
    private final LoginAddInfo loginAddInfo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
