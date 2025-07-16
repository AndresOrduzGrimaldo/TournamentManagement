package com.tournament.application.service;

import com.tournament.domain.entity.User;
import com.tournament.domain.repository.UserRepository;
import com.tournament.application.dto.LoginRequest;
import com.tournament.application.dto.RegisterRequest;
import com.tournament.application.dto.AuthResponse;
import com.tournament.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba usando builders
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(User.UserRole.PARTICIPANT)
                .isActive(true)
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("password123")
                .firstName("New")
                .lastName("User")
                .build();
    }

    @Test
    void testLogin_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken((User) testUser)).thenReturn("test.jwt.token");

        // Act
        AuthResponse result = authService.login(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test.jwt.token", result.getToken());
        assertNotNull(result.getUser());
        assertEquals("testuser", result.getUser().getUsername());
        assertEquals("Test", result.getUser().getFirstName());
        assertEquals("User", result.getUser().getLastName());
        assertEquals("PARTICIPANT", result.getUser().getRole());

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider).generateToken((User) testUser);
    }

    @Test
    void testLogin_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void testLogin_InvalidPassword() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.login(loginRequest);
        });

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void testRegister_Success() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("test.jwt.token");

        // Act
        AuthResponse result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test.jwt.token", result.getToken());
        assertNotNull(result.getUser());
        assertEquals("newuser", result.getUser().getUsername());

        verify(userRepository).findByUsername("newuser");
        verify(userRepository).findByEmail("new@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateToken(any(User.class));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository).findByUsername("newuser");
        verify(userRepository, never()).findByEmail(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Arrange
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(registerRequest);
        });

        verify(userRepository).findByUsername("newuser");
        verify(userRepository).findByEmail("new@example.com");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testValidateToken_ValidToken() {
        // Arrange
        String token = "valid.jwt.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.extractUsername(token)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        boolean result = authService.validateToken(token);

        // Assert
        assertTrue(result);

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).extractUsername(token);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Arrange
        String token = "invalid.jwt.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // Act
        boolean result = authService.validateToken(token);

        // Assert
        assertFalse(result);

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider, never()).extractUsername(any());
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void testValidateToken_UserNotFound() {
        // Arrange
        String token = "valid.jwt.token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.extractUsername(token)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act
        boolean result = authService.validateToken(token);

        // Assert
        assertFalse(result);

        verify(jwtTokenProvider).validateToken(token);
        verify(jwtTokenProvider).extractUsername(token);
        verify(userRepository).findByUsername("testuser");
    }
} 