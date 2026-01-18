package com.scaler.userservice_mar2025.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ApplicationConfig {

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpsecurity) throws Exception {
        httpsecurity.csrf(csrf ->csrf.disable())

        .cors(cors-> cors.disable())

       .authorizeHttpRequests(
                authorize -> authorize.anyRequest().permitAll()
        );
        return httpsecurity.build();
    }
}
