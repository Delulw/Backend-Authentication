package com.delulw.backendauthentication.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;

@Configuration
public class UserAuthConfiguration {

    @Bean
    AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            @Value("${security.auth.username}") String username,
            @Value("${security.auth.password}") String password,
            @Value("${security.auth.roles:USER}") String configuredRoles,
            PasswordEncoder passwordEncoder) {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("AUTH_USERNAME and AUTH_PASSWORD must not be blank");
        }
        String[] roles = Arrays.stream(configuredRoles.split(","))
                .map(String::trim)
                .toArray(String[]::new);
        if (roles.length == 0 || Arrays.stream(roles).anyMatch(role ->
                role.isBlank() ||
                !role.matches("[A-Z][A-Z0-9_]*") || role.startsWith("ROLE_"))) {
            throw new IllegalStateException(
                    "AUTH_ROLES must be comma-separated role names without the ROLE_ prefix");
        }
        UserDetails user = User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles(roles)
                .build();
        return new InMemoryUserDetailsManager(user);
    }

}
