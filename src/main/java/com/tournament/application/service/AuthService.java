package com.tournament.application.service;

import com.tournament.application.dto.AuthResponse;
import com.tournament.application.dto.LoginRequest;
import com.tournament.application.dto.RegisterRequest;
import com.tournament.domain.entity.User;
import com.tournament.domain.repository.UserRepository;
import com.tournament.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de autenticación
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra un nuevo usuario
     * @param request Solicitud de registro
     * @return Respuesta de autenticación
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getUsername());

        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Crear nuevo usuario
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(User.UserRole.valueOf(request.getRole()))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Usuario registrado exitosamente: {}", savedUser.getId());

        // Generar token
        String token = jwtTokenProvider.generateToken(savedUser);
        long expiresIn = jwtTokenProvider.getTimeUntilExpiration(token);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .user(mapToUserInfo(savedUser))
                .build();
    }

    /**
     * Autentica un usuario
     * @param request Solicitud de login
     * @return Respuesta de autenticación
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Autenticando usuario: {}", request.getUsername());

        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Obtener usuario de la base de datos
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar que el usuario esté activo
        if (!user.getIsActive()) {
            throw new IllegalArgumentException("Usuario inactivo");
        }

        // Generar token
        String token = jwtTokenProvider.generateToken(user);
        long expiresIn = jwtTokenProvider.getTimeUntilExpiration(token);

        log.info("Usuario autenticado exitosamente: {}", user.getId());

        return AuthResponse.builder()
                .token(token)
                .expiresIn(expiresIn)
                .user(mapToUserInfo(user))
                .build();
    }

    /**
     * Valida un token JWT
     * @param token Token JWT
     * @return true si el token es válido
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * Obtiene información del usuario desde un token
     * @param token Token JWT
     * @return Información del usuario
     */
    public AuthResponse.UserInfo getUserInfoFromToken(String token) {
        if (!validateToken(token)) {
            throw new IllegalArgumentException("Token inválido");
        }

        String username = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return mapToUserInfo(user);
    }

    /**
     * Refresca un token JWT
     * @param token Token JWT actual
     * @return Nuevo token JWT
     */
    public AuthResponse refreshToken(String token) {
        if (!validateToken(token)) {
            throw new IllegalArgumentException("Token inválido");
        }

        String username = jwtTokenProvider.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Generar nuevo token
        String newToken = jwtTokenProvider.generateToken(user);
        long expiresIn = jwtTokenProvider.getTimeUntilExpiration(newToken);

        return AuthResponse.builder()
                .token(newToken)
                .expiresIn(expiresIn)
                .user(mapToUserInfo(user))
                .build();
    }

    /**
     * Mapea una entidad User a UserInfo
     * @param user Entidad User
     * @return UserInfo
     */
    private AuthResponse.UserInfo mapToUserInfo(User user) {
        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }
} 