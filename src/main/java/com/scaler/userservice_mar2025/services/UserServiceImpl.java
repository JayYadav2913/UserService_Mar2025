package com.scaler.userservice_mar2025.services;

import com.scaler.userservice_mar2025.exceptions.InvalidTokenException;
import com.scaler.userservice_mar2025.exceptions.PasswordMismatchException;
import com.scaler.userservice_mar2025.models.Token;
import com.scaler.userservice_mar2025.models.User;
import com.scaler.userservice_mar2025.repositories.TokenRepository;
import com.scaler.userservice_mar2025.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private TokenRepository tokenRepository;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenRepository = tokenRepository;

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
    public Token login(String email, String password) throws PasswordMismatchException {
        Optional<User> optionalUser=userRepository.findByEmail(email);

        if(optionalUser.isEmpty()) {
            //redirect the user to the signUp page.
            return null;
        }
        User user =optionalUser.get();

        if(!bCryptPasswordEncoder.matches(password,user.getPassword())) {
            //Login unsuccessful
            throw new PasswordMismatchException("Incorrect password.");
        }
        // Login successful
        //Generate the Token
        Token token = new Token();
        token.setUser(user);

        //
        token.setTokenValue(RandomStringUtils.randomAlphanumeric(128));

        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,30);
        Date expiryDate = calendar.getTime();
        token.setExpiryAt(expiryDate);

        return tokenRepository.save(token);
    }

    @Override
    public User validateToken(String tokenValue) throws InvalidTokenException {

        Optional<Token> optionalToken = tokenRepository.findByTokenValueAndExpiryAtGreaterThan(
                tokenValue,
                new Date());

        if(optionalToken.isEmpty()){
            //Invalid Token
            throw new InvalidTokenException("Invalid token.");
        }

        //Token is valid
        Token token = optionalToken.get();
        return token.getUser();
    }

    @Override
    public void logout() {

    }
}
