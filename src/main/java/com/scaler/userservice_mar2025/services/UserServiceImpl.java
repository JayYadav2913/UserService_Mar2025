package com.scaler.userservice_mar2025.services;

import com.scaler.userservice_mar2025.models.Token;
import com.scaler.userservice_mar2025.models.User;
import com.scaler.userservice_mar2025.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User signUp() {
        return null;
    }

    @Override
    public Token login() {
        return null;
    }

    @Override
    public User validateToken() {
        return null;
    }

    @Override
    public void logout() {

    }
}
