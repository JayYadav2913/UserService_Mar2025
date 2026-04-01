package com.scaler.userservice_mar2025.configs;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import java.util.Base64;

@Configuration
public class ApplicationConfig {

    // Injected from application.properties — fixed value that survives restarts
    @Value("${jwt.secret.key}")
    private String jwtSecretKey;

    @Bean
    public BCryptPasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpsecurity) throws Exception {
        httpsecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(
                        authorize -> authorize.anyRequest().permitAll()
                );
        return httpsecurity.build();
    }

    @Bean
    public SecretKey getSecretKey() {
        // Decode the fixed base64 key from application.properties.
        // The same key is returned on every startup — tokens issued before
        // a restart remain valid as long as they have not expired.
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}