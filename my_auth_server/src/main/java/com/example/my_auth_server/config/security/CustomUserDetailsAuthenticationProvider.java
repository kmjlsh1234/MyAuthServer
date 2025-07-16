package com.example.my_auth_server.config.security;

import com.example.my_auth_server.user.constants.LoginType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
public class CustomUserDetailsAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private PasswordEncoder passwordEncoder;
    private UserDetailsService userDetailsService;


    public CustomUserDetailsAuthenticationProvider() {

    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (((CustomAuthenticationToken) authentication).getLoginAddInfo().getLoginType() == LoginType.SOCIAL) {
            return;
        }

        if(authentication.getCredentials() == null) {
            throw new BadCredentialsException("Bad credentials");
        }

        String presentPassword = authentication.getCredentials().toString();
        if(!passwordEncoder.matches(presentPassword, userDetails.getPassword())) {
            throw new BadCredentialsException(
                    messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        CustomAuthenticationToken auth = (CustomAuthenticationToken) authentication;
        UserDetails loadUser;
        try{
            loadUser = userDetailsService.loadUserByUsernameAndDomain(auth.getPrincipal().toString(), auth.getLoginAddInfo());
        } catch(UsernameNotFoundException notFound){
            throw notFound;
        }

        if(loadUser == null){
            throw new InternalAuthenticationServiceException("UserDetailsService returned null, "
                    + "which is an interface contract violation");
        }

        return loadUser;
    }
}
