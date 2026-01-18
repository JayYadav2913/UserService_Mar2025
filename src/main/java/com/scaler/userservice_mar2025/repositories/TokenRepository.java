package com.scaler.userservice_mar2025.repositories;

import com.scaler.userservice_mar2025.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,Long> {


    Optional<Token> findByTokenValue(String tokenValue);

    Token save(Token token);

    //validate the token.
    //Check if the token is present in the table with the given value
    //now check if expiry of the token > current time.
    Optional<Token> findByTokenValueAndExpiryAtGreaterThan(
            String tokenValue,
            Date expiryAt);
}
