package com.tournament.infrastructure.security;

import com.tournament.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtTokenProvider.setJwtSecret("testSecretKeyForTestingPurposesOnly12345678901234567890");
        jwtTokenProvider.setJwtExpiration(86400000L); // 24 horas

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole("PARTICIPANT");
    }

    @Test
    void testGenerateToken_Success() {
        // Act
        String token = jwtTokenProvider.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT tokens have 3 parts separated by dots
    }

    @Test
    void testValidateToken_ValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken(testUser);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_EmptyToken() {
        // Arrange
        String emptyToken = "";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_NullToken() {
        // Arrange
        String nullToken = null;

        // Act
        boolean isValid = jwtTokenProvider.validateToken(nullToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGetUsernameFromToken_ValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken(testUser);

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void testGetUsernameFromToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    void testTokenExpiration() {
        // Arrange - Create token provider with short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        shortExpirationProvider.setJwtSecret("testSecretKeyForTestingPurposesOnly12345678901234567890");
        shortExpirationProvider.setJwtExpiration(1L); // 1 millisecond

        // Act
        String token = shortExpirationProvider.generateToken(testUser);

        // Wait for token to expire
        try {
            Thread.sleep(10); // Wait 10 milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        boolean isValid = shortExpirationProvider.validateToken(token);
        assertFalse(isValid);
    }
} 