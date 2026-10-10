package com.workwave.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** After login, send each role to its own dashboard. */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            String target = "/";
            for (GrantedAuthority a : authentication.getAuthorities()) {
                switch (a.getAuthority()) {
                    case "ROLE_ADMIN" -> target = "/admin/dashboard";
                    case "ROLE_EMPLOYER" -> target = "/employer/dashboard";
                    case "ROLE_JOB_SEEKER" -> target = "/seeker/dashboard";
                    default -> { }
                }
            }
            response.sendRedirect(request.getContextPath() + target);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler)
            throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/jobs", "/jobs/**", "/register", "/login",
                                 "/css/**", "/js/**", "/error", "/h2-console/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/employer/**").hasRole("EMPLOYER")
                .requestMatchers("/seeker/**").hasRole("JOB_SEEKER")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .successHandler(successHandler)
                .permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll())
            // H2 console (dev only) needs frames + no CSRF
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
