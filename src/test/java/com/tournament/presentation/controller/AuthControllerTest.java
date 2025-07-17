package com.tournament.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tournament.application.dto.LoginRequest;
import com.tournament.application.dto.RegisterRequest;
import com.tournament.application.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitario simple para AuthController que no depende del contexto de Spring
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        loginRequest = new LoginRequest();
        ReflectionTestUtils.setField(loginRequest, "username", "testuser");
        ReflectionTestUtils.setField(loginRequest, "password", "password123");

        registerRequest = new RegisterRequest();
        ReflectionTestUtils.setField(registerRequest, "username", "newuser");
        ReflectionTestUtils.setField(registerRequest, "password", "password123");
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(createMockAuthResponse());

        // Act
        var response = authController.login(loginRequest);

        // Assert
        verify(authService).login(loginRequest);
        assert response.getStatusCode().is2xxSuccessful();
    }

    @Test
    void testLogin_ServiceThrowsException() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        // Act
        var response = authController.login(loginRequest);

        // Assert
        verify(authService).login(loginRequest);
        assert response.getStatusCode().is4xxClientError();
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(createMockAuthResponse());

        // Act
        var response = authController.register(registerRequest);

        // Assert
        verify(authService).register(registerRequest);
        assert response.getStatusCode().is2xxSuccessful();
    }

    @Test
    void testRegister_ServiceThrowsException() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("User already exists"));

        // Act
        var response = authController.register(registerRequest);

        // Assert
        verify(authService).register(registerRequest);
        assert response.getStatusCode().is4xxClientError();
    }

    @Test
    void testValidateToken_Success() throws Exception {
        // Arrange
        String token = "valid.jwt.token";
        when(authService.getUserInfoFromToken(token)).thenReturn(createMockUserInfo());

        // Act
        var response = authController.validateToken(token);

        // Assert
        verify(authService).getUserInfoFromToken(token);
        assert response.getStatusCode().is2xxSuccessful();
    }

    @Test
    void testValidateToken_InvalidToken() throws Exception {
        // Arrange
        String token = "invalid.jwt.token";
        when(authService.getUserInfoFromToken(token)).thenThrow(new IllegalArgumentException("Invalid token"));

        // Act
        var response = authController.validateToken(token);

        // Assert
        verify(authService).getUserInfoFromToken(token);
        assert response.getStatusCode().is4xxClientError();
    }

    private com.tournament.application.dto.AuthResponse createMockAuthResponse() {
        com.tournament.application.dto.AuthResponse response = new com.tournament.application.dto.AuthResponse();
        ReflectionTestUtils.setField(response, "token", "test.jwt.token");
        
        com.tournament.application.dto.AuthResponse.UserInfo userInfo = new com.tournament.application.dto.AuthResponse.UserInfo();
        ReflectionTestUtils.setField(userInfo, "id", 1L);
        ReflectionTestUtils.setField(userInfo, "username", "testuser");
        ReflectionTestUtils.setField(userInfo, "email", "test@example.com");
        ReflectionTestUtils.setField(userInfo, "firstName", "Test");
        ReflectionTestUtils.setField(userInfo, "lastName", "User");
        ReflectionTestUtils.setField(userInfo, "role", "PARTICIPANT");
        ReflectionTestUtils.setField(userInfo, "fullName", "Test User");
        
        ReflectionTestUtils.setField(response, "user", userInfo);
        return response;
    }

    private com.tournament.application.dto.AuthResponse.UserInfo createMockUserInfo() {
        com.tournament.application.dto.AuthResponse.UserInfo userInfo = new com.tournament.application.dto.AuthResponse.UserInfo();
        ReflectionTestUtils.setField(userInfo, "id", 1L);
        ReflectionTestUtils.setField(userInfo, "username", "testuser");
        ReflectionTestUtils.setField(userInfo, "email", "test@example.com");
        ReflectionTestUtils.setField(userInfo, "firstName", "Test");
        ReflectionTestUtils.setField(userInfo, "lastName", "User");
        ReflectionTestUtils.setField(userInfo, "role", "PARTICIPANT");
        ReflectionTestUtils.setField(userInfo, "fullName", "Test User");
        return userInfo;
    }
} 