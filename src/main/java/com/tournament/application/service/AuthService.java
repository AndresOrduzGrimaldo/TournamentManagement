package com.tournament.application.service;

import com.tournament.application.dto.AuthResponse;
import com.tournament.application.dto.LoginRequest;
import com.tournament.application.dto.RegisterRequest;
import com.tournament.domain.entity.User;
import com.tournament.domain.repository.UserRepository;
import com.tournament.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de aplicación para la autenticación de usuarios
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Autentica un usuario y genera un token JWT
     * @param request Solicitud de login
     * @return Respuesta de autenticación
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Intentando autenticar usuario: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Contraseña incorrecta para usuario: {}", request.getUsername());
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        if (!user.getIsActive()) {
            log.warn("Usuario inactivo intentando autenticarse: {}", request.getUsername());
            throw new IllegalArgumentException("Usuario inactivo");
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("Usuario autenticado exitosamente: {}", request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Registra un nuevo usuario
     * @param request Solicitud de registro
     * @return Respuesta de autenticación
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Intentando registrar nuevo usuario: {}", request.getUsername());

        // Verificar si el username ya existe
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Intento de registro con username existente: {}", request.getUsername());
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        // Verificar si el email ya existe
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Intento de registro con email existente: {}", request.getEmail());
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Crear nuevo usuario
        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(User.UserRole.PARTICIPANT)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(newUser);
        String token = jwtTokenProvider.generateToken(savedUser);

        log.info("Usuario registrado exitosamente: {}", request.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole().name())
                .build();
    }

    /**
     * Valida un token JWT
     * @param token Token a validar
     * @return true si el token es válido
     */
    public boolean validateToken(String token) {
        log.debug("Validando token JWT");
        
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Token JWT inválido");
            return false;
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isEmpty()) {
            log.warn("Usuario no encontrado para token válido: {}", username);
            return false;
        }

        if (!user.get().getIsActive()) {
            log.warn("Usuario inactivo con token válido: {}", username);
            return false;
        }

        log.debug("Token JWT válido para usuario: {}", username);
        return true;
    }

    /**
     * Refresca un token JWT
     * @param token Token actual
     * @return Nuevo token
     */
    public AuthResponse refreshToken(String token) {
        log.info("Refrescando token JWT");

        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Token inválido");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        String newToken = jwtTokenProvider.generateToken(user);
        log.info("Token refrescado exitosamente para usuario: {}", username);

        return AuthResponse.builder()
                .token(newToken)
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .build();
    }

    /**
     * Cierra la sesión de un usuario (logout)
     * @param token Token a invalidar
     */
    public void logout(String token) {
        log.info("Cerrando sesión de usuario");
        
        // En una implementación real, aquí se invalidaría el token
        // Por ahora, solo registramos la acción
        if (jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            log.info("Sesión cerrada para usuario: {}", username);
        }
    }
} 