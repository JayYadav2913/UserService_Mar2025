package com.scaler.userservice_mar2025.services;

import com.scaler.userservice_mar2025.models.Token;
import com.scaler.userservice_mar2025.models.User;
import com.scaler.userservice_mar2025.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
    }


    @Override
    public User signUp(String name, String email, String password) {
        //Check if there's already a user with the email or not.
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isPresent()) {
            //redirect to the login page
            return optionalUser.get();
        }
        User user = new User();
        user.setEmail(email);
        user.setName(name);

        //Use Bcrypt PasswordEncoder to encode password
      //  BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        user.setPassword(bCryptPasswordEncoder.encode(password));
        return userRepository.save(user);

    }

    @Override
    public Token login(String email, String password) {
        return null;
    }

    @Override
    public User validateToken(String tokenValue) {
        return null;
    }

    @Override
    public void logout() {

    }
}
