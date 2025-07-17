package com.tournament.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tournament.application.dto.LoginRequest;
import com.tournament.application.dto.RegisterRequest;
import com.tournament.domain.entity.User;
import com.tournament.domain.repository.UserRepository;
import com.tournament.infrastructure.security.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AuthController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@EnableJpaAuditing
@Import(TestSecurityConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "email", "test@example.com");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "firstName", "Test");
        ReflectionTestUtils.setField(request, "lastName", "User");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()) // 201 es correcto para registro exitoso
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("Test"))
                .andExpect(jsonPath("$.user.lastName").value("User"));

        // Verificar que el usuario se guardó en la base de datos
        User savedUser = userRepository.findByUsername("testuser").orElse(null);
        assert savedUser != null;
        assert ReflectionTestUtils.getField(savedUser, "email").equals("test@example.com");
    }

    @Test
    void testRegister_DuplicateUsername() throws Exception {
        // Arrange - Crear usuario existente
        User existingUser = new User();
        ReflectionTestUtils.setField(existingUser, "username", "testuser");
        ReflectionTestUtils.setField(existingUser, "email", "existing@example.com");
        ReflectionTestUtils.setField(existingUser, "passwordHash", "$2a$10$xJ6wyYCWnXdBJCX2fL2h.u0R7EqTB.nSyg3liLw0J4Br/cVpXzZRS"); // password123
        ReflectionTestUtils.setField(existingUser, "firstName", "Existing");
        ReflectionTestUtils.setField(existingUser, "lastName", "User");
        ReflectionTestUtils.setField(existingUser, "role", User.UserRole.PARTICIPANT);
        ReflectionTestUtils.setField(existingUser, "isActive", true);
        ReflectionTestUtils.setField(existingUser, "createdAt", LocalDateTime.now());
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        ReflectionTestUtils.setField(request, "username", "testuser"); // Username duplicado
        ReflectionTestUtils.setField(request, "email", "new@example.com");
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "firstName", "Test");
        ReflectionTestUtils.setField(request, "lastName", "User");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 400 para error de validación
    }

    @Test
    void testRegister_DuplicateEmail() throws Exception {
        // Arrange - Crear usuario existente
        User existingUser = new User();
        ReflectionTestUtils.setField(existingUser, "username", "existinguser");
        ReflectionTestUtils.setField(existingUser, "email", "test@example.com"); // Email duplicado
        ReflectionTestUtils.setField(existingUser, "passwordHash", "$2a$10$xJ6wyYCWnXdBJCX2fL2h.u0R7EqTB.nSyg3liLw0J4Br/cVpXzZRS"); // password123
        ReflectionTestUtils.setField(existingUser, "firstName", "Existing");
        ReflectionTestUtils.setField(existingUser, "lastName", "User");
        ReflectionTestUtils.setField(existingUser, "role", User.UserRole.PARTICIPANT);
        ReflectionTestUtils.setField(existingUser, "isActive", true);
        ReflectionTestUtils.setField(existingUser, "createdAt", LocalDateTime.now());
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        ReflectionTestUtils.setField(request, "username", "newuser");
        ReflectionTestUtils.setField(request, "email", "test@example.com"); // Email duplicado
        ReflectionTestUtils.setField(request, "password", "password123");
        ReflectionTestUtils.setField(request, "firstName", "Test");
        ReflectionTestUtils.setField(request, "lastName", "User");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 400 para error de validación
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange - Crear usuario con contraseña correctamente hasheada
        User user = new User();
        ReflectionTestUtils.setField(user, "username", "testuser");
        ReflectionTestUtils.setField(user, "email", "test@example.com");
        // Usar un hash BCrypt válido para "password123"
        ReflectionTestUtils.setField(user, "passwordHash", "$2a$10$xJ6wyYCWnXdBJCX2fL2h.u0R7EqTB.nSyg3liLw0J4Br/cVpXzZRS");
        ReflectionTestUtils.setField(user, "firstName", "Test");
        ReflectionTestUtils.setField(user, "lastName", "User");
        ReflectionTestUtils.setField(user, "role", User.UserRole.PARTICIPANT);
        ReflectionTestUtils.setField(user, "isActive", true);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "password", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.firstName").value("Test"))
                .andExpect(jsonPath("$.user.lastName").value("User"))
                .andExpect(jsonPath("$.user.role").value("PARTICIPANT"));
    }

    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "username", "nonexistent");
        ReflectionTestUtils.setField(request, "password", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // 401 para credenciales inválidas
    }

    @Test
    void testLogin_WrongPassword() throws Exception {
        // Arrange - Crear usuario
        User user = new User();
        ReflectionTestUtils.setField(user, "username", "testuser");
        ReflectionTestUtils.setField(user, "email", "test@example.com");
        ReflectionTestUtils.setField(user, "passwordHash", "$2a$10$xJ6wyYCWnXdBJCX2fL2h.u0R7EqTB.nSyg3liLw0J4Br/cVpXzZRS"); // password123
        ReflectionTestUtils.setField(user, "firstName", "Test");
        ReflectionTestUtils.setField(user, "lastName", "User");
        ReflectionTestUtils.setField(user, "role", User.UserRole.PARTICIPANT);
        ReflectionTestUtils.setField(user, "isActive", true);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "username", "testuser");
        ReflectionTestUtils.setField(request, "password", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // 401 para credenciales inválidas
    }

    @Test
    void testValidateToken_ValidToken() throws Exception {
        // Arrange - Crear usuario y obtener token
        User user = new User();
        ReflectionTestUtils.setField(user, "username", "testuser");
        ReflectionTestUtils.setField(user, "email", "test@example.com");
        ReflectionTestUtils.setField(user, "passwordHash", "$2a$10$xJ6wyYCWnXdBJCX2fL2h.u0R7EqTB.nSyg3liLw0J4Br/cVpXzZRS");
        ReflectionTestUtils.setField(user, "firstName", "Test");
        ReflectionTestUtils.setField(user, "lastName", "User");
        ReflectionTestUtils.setField(user, "role", User.UserRole.PARTICIPANT);
        ReflectionTestUtils.setField(user, "isActive", true);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());
        userRepository.save(user);

        // Primero hacer login para obtener token
        LoginRequest loginRequest = new LoginRequest();
        ReflectionTestUtils.setField(loginRequest, "username", "testuser");
        ReflectionTestUtils.setField(loginRequest, "password", "password123");

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extraer token de la respuesta
        String token = objectMapper.readTree(response).get("token").asText();

        // Act & Assert - Validar token usando parámetro en lugar de header
        mockMvc.perform(post("/api/auth/validate")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testValidateToken_InvalidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/validate")
                .param("token", "invalid-token"))
                .andExpect(status().isUnauthorized()); // 401 para token inválido
    }

    @Test
    void testValidateToken_NoToken() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/validate"))
                .andExpect(status().isBadRequest()); // 400 para parámetro faltante
    }
} 