package com.scaler.userservice_mar2025;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UserServiceMar2025Application {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceMar2025Application.class, args);
    }

}
