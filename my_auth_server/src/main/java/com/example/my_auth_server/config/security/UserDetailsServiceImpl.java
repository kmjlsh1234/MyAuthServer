package com.example.my_auth_server.config.security;

import com.example.my_auth_server.user.constants.LoginType;
import com.example.my_auth_server.user.model.LoginAddInfo;
import com.example.my_auth_server.user.model.Users;
import com.example.my_auth_server.user.repository.UserRepository;
import com.example.my_auth_server.user.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsernameAndDomain(String id, LoginAddInfo loginAddInfo) throws UsernameNotFoundException {

        if(loginAttemptService.isBlocked(id)){
            throw new UsernameNotFoundException("Username is blocked");
        }

        Users user = getUser(loginAddInfo.getLoginType(), id);

        return UserPrincipal.builder()
                .userId(user.getUserId())
                .loginId(user.getLoginId())
                .loginType(user.getLoginType())
                .password(user.getPassword())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .loginAddInfo(loginAddInfo)
                .build();
    }

    private Users getUser(LoginType loginType, String id) {
        Optional<Users> user = switch(loginType){
            case GUEST -> userRepository.findByLoginId(id);
            case EMAIL, SOCIAL -> userRepository.findByEmail(id);
            case MOBILE -> userRepository.findByMobile(id);
            default -> Optional.empty();
        };

        return user.orElseThrow(() -> new UsernameNotFoundException(id));
    }
}
