package com.thingverse.api.security.config;

import com.thingverse.api.services.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class JwtUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userService.getUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found"));
    }
}
