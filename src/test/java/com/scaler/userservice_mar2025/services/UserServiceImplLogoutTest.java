package com.scaler.userservice_mar2025.services;

import com.scaler.userservice_mar2025.exceptions.InvalidTokenException;
import com.scaler.userservice_mar2025.models.State;
import com.scaler.userservice_mar2025.models.Token;
import com.scaler.userservice_mar2025.models.User;
import com.scaler.userservice_mar2025.repositories.TokenRepository;
import com.scaler.userservice_mar2025.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplLogoutTest {

    // ── Mocks ──────────────────────────────────────────────────────────────
    @Mock private UserRepository        userRepository;
    @Mock private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock private TokenRepository       tokenRepository;
    @Mock private SecretKey             secretKey;

    @InjectMocks private UserServiceImpl userService;

    // ── Test data ──────────────────────────────────────────────────────────
    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9.test.signature";

    private Token activeToken;
    private Token deletedToken;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@scaler.com");

        activeToken = new Token();
        activeToken.setId(1L);
        activeToken.setTokenValue(VALID_TOKEN);
        activeToken.setUser(user);
        activeToken.setExpiryAt(new Date(System.currentTimeMillis() + 86_400_000L)); // 1 day from now
        activeToken.setState(State.ACTIVE);

        deletedToken = new Token();
        deletedToken.setId(2L);
        deletedToken.setTokenValue(VALID_TOKEN);
        deletedToken.setUser(user);
        deletedToken.setExpiryAt(new Date(System.currentTimeMillis() + 86_400_000L));
        deletedToken.setState(State.DELETED);
    }


    // ── TEST 1: Happy path — active token is successfully invalidated ──────
    @Test
    void logout_WithValidActiveToken_ShouldMarkTokenAsDeleted()
            throws InvalidTokenException {

        // Arrange
        when(tokenRepository.findByTokenValue(VALID_TOKEN))
                .thenReturn(Optional.of(activeToken));

        // Act
        userService.logout(VALID_TOKEN);

        // Assert — capture what was saved to the DB
        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository, times(1)).save(tokenCaptor.capture());

        Token savedToken = tokenCaptor.getValue();
        assertEquals(State.DELETED, savedToken.getState(),
                "Token state must be DELETED after logout");
        assertEquals(VALID_TOKEN, savedToken.getTokenValue(),
                "Saved token value must match the one passed in");
    }


    // ── TEST 2: Token not in DB — should throw InvalidTokenException ───────
    @Test
    void logout_WithTokenNotInDatabase_ShouldThrowInvalidTokenException() {

        // Arrange
        when(tokenRepository.findByTokenValue(VALID_TOKEN))
                .thenReturn(Optional.empty());

        // Act + Assert
        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> userService.logout(VALID_TOKEN)
        );

        assertTrue(exception.getMessage().contains("not found"),
                "Exception message should mention token not found");

        // DB save should never be called
        verify(tokenRepository, never()).save(any(Token.class));
    }


    // ── TEST 3: Token already DELETED — idempotent (no exception, no save) ─
    @Test
    void logout_WithAlreadyDeletedToken_ShouldReturnWithoutException()
            throws InvalidTokenException {

        // Arrange
        when(tokenRepository.findByTokenValue(VALID_TOKEN))
                .thenReturn(Optional.of(deletedToken));

        // Act — should not throw
        assertDoesNotThrow(() -> userService.logout(VALID_TOKEN));

        // Assert — save should NOT be called again (token already invalidated)
        verify(tokenRepository, never()).save(any(Token.class));
    }


    // ── TEST 4: Null token value — should throw ───────────────────────────
    @Test
    void logout_WithNullToken_ShouldThrowInvalidTokenException() {

        // Arrange
        when(tokenRepository.findByTokenValue(null))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                InvalidTokenException.class,
                () -> userService.logout(null)
        );

        verify(tokenRepository, never()).save(any(Token.class));
    }


    // ── TEST 5: Empty string token — should throw ─────────────────────────
    @Test
    void logout_WithEmptyToken_ShouldThrowInvalidTokenException() {

        // Arrange
        when(tokenRepository.findByTokenValue(""))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(
                InvalidTokenException.class,
                () -> userService.logout("")
        );
    }


    // ── TEST 6: tokenRepository.save() is called with correct user ────────
    @Test
    void logout_ShouldPreserveUserAssociationOnSave()
            throws InvalidTokenException {

        // Arrange
        when(tokenRepository.findByTokenValue(VALID_TOKEN))
                .thenReturn(Optional.of(activeToken));

        // Act
        userService.logout(VALID_TOKEN);

        // Assert — user association must not be changed during logout
        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());

        assertNotNull(captor.getValue().getUser(),
                "User association must be preserved after logout");
        assertEquals(1L, captor.getValue().getUser().getId(),
                "User ID must remain unchanged after logout");
    }
}