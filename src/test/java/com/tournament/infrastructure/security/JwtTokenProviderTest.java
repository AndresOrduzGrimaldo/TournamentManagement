package com.tournament.infrastructure.security;

import com.tournament.domain.entity.User;
import com.tournament.domain.entity.User.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class JwtTokenProviderTest {

    private static final String TEST_SECRET = "1234567890123456789012345678901234567890123456789012345678901234";

    private JwtTokenProvider jwtTokenProvider;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L); // 24 horas

        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        ReflectionTestUtils.setField(testUser, "username", "testuser");
        ReflectionTestUtils.setField(testUser, "email", "test@example.com");
        ReflectionTestUtils.setField(testUser, "role", UserRole.PARTICIPANT);
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
    void testExtractUsername_ValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken(testUser);

        // Act
        String username = jwtTokenProvider.extractUsername(token);

        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void testExtractUsername_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.extractUsername(invalidToken);
        });
    }

    @Test
    void testTokenExpiration() {
        // Arrange - Create token provider with short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(shortExpirationProvider, "jwtExpiration", 1L); // 1 millisecond

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