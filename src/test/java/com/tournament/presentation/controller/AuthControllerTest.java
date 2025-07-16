package com.tournament.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tournament.application.dto.LoginRequest;
import com.tournament.application.dto.RegisterRequest;
import com.tournament.application.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(createMockAuthResponse());

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testLogin_InvalidRequest() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(createMockAuthResponse());

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testRegister_InvalidRequest() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testValidateToken_Success() throws Exception {
        // Arrange
        String token = "valid.jwt.token";
        when(authService.validateToken(token)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testValidateToken_InvalidToken() throws Exception {
        // Arrange
        String token = "invalid.jwt.token";
        when(authService.validateToken(token)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    private com.tournament.application.dto.AuthResponse createMockAuthResponse() {
        return com.tournament.application.dto.AuthResponse.builder()
                .token("test.jwt.token")
                .user(com.tournament.application.dto.AuthResponse.UserInfo.builder()
                        .id(1L)
                        .username("testuser")
                        .email("test@example.com")
                        .firstName("Test")
                        .lastName("User")
                        .role("PARTICIPANT")
                        .fullName("Test User")
                        .build())
                .build();
    }
} 