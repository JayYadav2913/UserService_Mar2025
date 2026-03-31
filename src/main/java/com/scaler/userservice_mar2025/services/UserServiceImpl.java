package com.scaler.userservice_mar2025.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.userservice_mar2025.dtos.SendEmailDto;
import com.scaler.userservice_mar2025.exceptions.InvalidTokenException;
import com.scaler.userservice_mar2025.exceptions.PasswordMismatchException;
import com.scaler.userservice_mar2025.models.State;
import com.scaler.userservice_mar2025.models.Token;
import com.scaler.userservice_mar2025.models.User;
import com.scaler.userservice_mar2025.repositories.TokenRepository;
import com.scaler.userservice_mar2025.repositories.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private TokenRepository tokenRepository;
    private SecretKey secretKey;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;


    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           TokenRepository tokenRepository,
                           SecretKey secretKey,
                           KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenRepository = tokenRepository;
        this.secretKey = secretKey;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper= objectMapper;

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

        User savedUser = userRepository.save(user);



        //Push a message to kafka to send a welcome email.

        SendEmailDto emailDto =new SendEmailDto();
        emailDto.setEmail(email);
        emailDto.setSubject("Welcome to Scaler!");
        emailDto.setBody("Welcome and new registration is done.");


        try {
            kafkaTemplate.send(
                    "sendWelcomeEmail",
                    objectMapper.writeValueAsString(emailDto)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return savedUser;
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
        // Build JWT claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss","scaler.com");
        claims.put("userId",user.getId());

        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,30);
        Date expiryDate = calendar.getTime();

        claims.put("exp",expiryDate);
        claims.put("roles",user.getRoles()
                .stream()
                .map(r -> r.getValue())
                .toList()); //store role strings



//        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
//        String token = Jwts.builder().content(payloadBytes).compact();

        String jwtToken=Jwts.builder().claims(claims).signWith(secretKey).compact();

        // Persist the token so logout() can invalidate it
        Token token=new Token();
        token.setTokenValue(jwtToken);
        token.setUser(user);
        token.setExpiryAt(expiryDate);
        token.setState(State.ACTIVE);
        tokenRepository.save(token);



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
            // Step 1 — cryptographic verification (signature + expiry)
            JwtParser jwtParser = Jwts.parser()
                    .verifyWith(secretKey)
                    .build();

            Claims claims = jwtParser
                    .parseSignedClaims(tokenValue)
                    .getPayload();

            // Step 2 — check DB: has this token been logged out?
            Optional<Token> optionalToken = tokenRepository.findByTokenValue(tokenValue);
            if (optionalToken.isEmpty()) {
                throw new InvalidTokenException("Token not Found");
            }

            Token token = optionalToken.get();

            // State.DELETED means user has explicitly logged out
            // BaseModel stores state; check it is still ACTIVE

            if (token.getState() != null &&
                    token.getState().toString().equals("DELETED")) {
                throw new InvalidTokenException("Token has been invalidated. Please log in again.");
            }


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
    public void logout(String tokenValue) throws InvalidTokenException {


        Optional<Token> optionalToken = tokenRepository.findByTokenValue(tokenValue);

        // 2. If token doesn't exist in DB it was never issued
        if (optionalToken.isEmpty()) {
            throw new InvalidTokenException(
                    "Token not found. It may have already been logged out or is invalid."
            );
        }

        Token token = optionalToken.get();

        // 3. Check it is not already invalidated
        if (token.getState() != null &&
                token.getState().toString().equals("DELETED")) {
            // Already logged out — treat as success
            return;
        }

        // 4. Soft-delete: mark the token as DELETED using the State enum

        token.setState(
                com.scaler.userservice_mar2025.models.State.DELETED
                // If State enum is in ProductCatalog only, see note below FILE 6
        );

        // 5. Persist the change
        tokenRepository.save(token);

    }
}

