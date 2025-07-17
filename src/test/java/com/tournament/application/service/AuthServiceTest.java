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
import org.springframework.test.util.ReflectionTestUtils;

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
        // Configurar datos de prueba usando reflection para evitar problemas de Lombok
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        ReflectionTestUtils.setField(testUser, "username", "testuser");
        ReflectionTestUtils.setField(testUser, "email", "test@example.com");
        ReflectionTestUtils.setField(testUser, "passwordHash", "encodedPassword");
        ReflectionTestUtils.setField(testUser, "firstName", "Test");
        ReflectionTestUtils.setField(testUser, "lastName", "User");
        ReflectionTestUtils.setField(testUser, "role", User.UserRole.PARTICIPANT);
        ReflectionTestUtils.setField(testUser, "isActive", true);

        loginRequest = new LoginRequest();
        ReflectionTestUtils.setField(loginRequest, "username", "testuser");
        ReflectionTestUtils.setField(loginRequest, "password", "password123");

        registerRequest = new RegisterRequest();
        ReflectionTestUtils.setField(registerRequest, "username", "newuser");
        ReflectionTestUtils.setField(registerRequest, "email", "new@example.com");
        ReflectionTestUtils.setField(registerRequest, "firstName", "New");
        ReflectionTestUtils.setField(registerRequest, "lastName", "User");
        ReflectionTestUtils.setField(registerRequest, "password", "password123");
        ReflectionTestUtils.setField(registerRequest, "role", "PARTICIPANT");
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
        assertEquals("test.jwt.token", ReflectionTestUtils.getField(result, "token"));
        assertNotNull(ReflectionTestUtils.getField(result, "user"));
        assertEquals("testuser", ReflectionTestUtils.getField(ReflectionTestUtils.getField(result, "user"), "username"));
        assertEquals("Test", ReflectionTestUtils.getField(ReflectionTestUtils.getField(result, "user"), "firstName"));
        assertEquals("User", ReflectionTestUtils.getField(ReflectionTestUtils.getField(result, "user"), "lastName"));
        assertEquals("PARTICIPANT", ReflectionTestUtils.getField(ReflectionTestUtils.getField(result, "user"), "role"));

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
        User newUser = new User();
        ReflectionTestUtils.setField(newUser, "id", 2L);
        ReflectionTestUtils.setField(newUser, "username", "newuser");
        ReflectionTestUtils.setField(newUser, "email", "new@example.com");
        ReflectionTestUtils.setField(newUser, "passwordHash", "encodedPassword");
        ReflectionTestUtils.setField(newUser, "firstName", "New");
        ReflectionTestUtils.setField(newUser, "lastName", "User");
        ReflectionTestUtils.setField(newUser, "role", User.UserRole.PARTICIPANT);
        ReflectionTestUtils.setField(newUser, "isActive", true);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("test.jwt.token");

        // Act
        AuthResponse result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test.jwt.token", ReflectionTestUtils.getField(result, "token"));
        assertNotNull(ReflectionTestUtils.getField(result, "user"));
        assertEquals("newuser", ReflectionTestUtils.getField(ReflectionTestUtils.getField(result, "user"), "username"));

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