package com.example.my_auth_server.config.security;

import com.example.my_auth_server.user.model.LoginAddInfo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface UserDetailsService {
    UserDetails loadUserByUsernameAndDomain(String id, LoginAddInfo loginAddInfo) throws UsernameNotFoundException;
}
