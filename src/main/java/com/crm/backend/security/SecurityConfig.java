package com.crm.backend.security;


import com.crm.backend.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] WHITE_LIST_URL = {
            "/api/v1/auth/**",
//            "/api/v1/users/**",
            "/api/v1/token/**" // this endpoint must be only accessible to the admin, for testing purposes it is in whitelist for now
    };
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(WHITE_LIST_URL)
                                .permitAll()
                                .requestMatchers("/api/v1/request/create/", "/api/v1/request/delete/",
                                        "/api/v1/request/rate/", "/api/v1/request/",

                                        "/api/v1/users/single/", "/api/v1/users/update/").hasAuthority(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/request/confirm/", "/api/v1/request/finish/",
                                        "/api/v1/request/",  "/api/v1/request/agent/dashboard/",

                                        "/api/v1/users/single/", "/api/v1/users/update/",
                                        "/api/v1/users/agent/change-status/").hasAuthority(Role.AGENT.name())
                                .requestMatchers("/api/v1/users/**", "/api/v1/request/reassign/",
                                        "/api/v1/request/", "/api/v1/request/all/").hasAuthority(Role.ADMIN.name())
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}