package com.scaler.userservice_mar2025.services;

import com.scaler.userservice_mar2025.exceptions.InvalidTokenException;
import com.scaler.userservice_mar2025.exceptions.PasswordMismatchException;
import com.scaler.userservice_mar2025.models.Token;
import com.scaler.userservice_mar2025.models.User;
import com.scaler.userservice_mar2025.repositories.TokenRepository;
import com.scaler.userservice_mar2025.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private TokenRepository tokenRepository;
    private SecretKey secretKey;

    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           TokenRepository tokenRepository,
                           SecretKey secretKey) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenRepository = tokenRepository;
        this.secretKey = secretKey;

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
    public String login(String email, String password) throws PasswordMismatchException {
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
//        Token token = new Token();
//        token.setUser(user);
//
//        //
//        token.setTokenValue(RandomStringUtils.randomAlphanumeric(128));
//
//
//        return tokenRepository.save(token);

        //Generate a JWT Token using JJWT library.

        //Hardcoded
//        String payload = "{\n" +
//                " \"email\":\"anil@gmail.com\",\n" +
//                " \"userId\":\"2\",\n" +
//                " \"roles\":[\"STUDENT\"],\n" +
//                " \"expiry\":\"2026-07-30T12:34:56Z\",\n" +
//                "}";

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss","scaler.com");
        claims.put("userId",user.getId());

        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,30);
        Date expiryDate = calendar.getTime();

        claims.put("exp",expiryDate);
        claims.put("roles",user.getRoles());

//        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
//        String token = Jwts.builder().content(payloadBytes).compact();

        String jwtToken=Jwts.builder().claims(claims).signWith(secretKey).compact();
        return jwtToken;

    }

    @Override
    public User validateToken(String tokenValue) throws InvalidTokenException {

//        Optional<Token> optionalToken = tokenRepository.findByTokenValueAndExpiryAtGreaterThan(
//                tokenValue,
//                new Date());
//
//        if(optionalToken.isEmpty()){
//            //Invalid Token
//            throw new InvalidTokenException("Invalid token.");
//        }
//
//        //Token is valid
//        Token token = optionalToken.get();
//        return token.getUser();

        try {
            JwtParser jwtParser = Jwts.parser()
                    .verifyWith(secretKey)
                    .build();

            Claims claims = jwtParser
                    .parseSignedClaims(tokenValue)
                    .getPayload();

            Object userIdObj = claims.get("userId");

            Long userId;
            if (userIdObj instanceof Integer) {
                userId = ((Integer) userIdObj).longValue();
            } else {
                userId = (Long) userIdObj;
            }

            return userRepository.findById(userId)
                    .orElseThrow(() -> new InvalidTokenException("User not found"));

        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired JWT token");
        }
    }

    @Override
    public void logout() {

    }
}
