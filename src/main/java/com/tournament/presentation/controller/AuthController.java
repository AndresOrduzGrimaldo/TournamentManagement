package com.tournament.presentation.controller;

import com.tournament.application.dto.AuthResponse;
import com.tournament.application.dto.LoginRequest;
import com.tournament.application.dto.RegisterRequest;
import com.tournament.application.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controlador para autenticación y autorización
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Autenticación", description = "API para autenticación y autorización")
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario
     */
    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o usuario ya existe"),
        @ApiResponse(responseCode = "409", description = "Conflicto: usuario o email ya existe")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Solicitud de registro para usuario: {}", request.getUsername());
        
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Error en registro: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Autentica un usuario
     */
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Solicitud de login para usuario: {}", request.getUsername());
        
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Valida un token JWT
     */
    @PostMapping("/validate")
    @Operation(summary = "Validar token", description = "Valida un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token válido"),
        @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<AuthResponse.UserInfo> validateToken(@RequestParam String token) {
        log.info("Validando token JWT");
        
        try {
            AuthResponse.UserInfo userInfo = authService.getUserInfoFromToken(token);
            return ResponseEntity.ok(userInfo);
        } catch (IllegalArgumentException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Refresca un token JWT
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Refresca un token JWT")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String token) {
        log.info("Refrescando token JWT");
        
        try {
            AuthResponse response = authService.refreshToken(token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Error refrescando token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Obtiene información del usuario actual
     */
    @GetMapping("/me")
    @Operation(summary = "Información del usuario", description = "Obtiene información del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Información del usuario obtenida",
                    content = @Content(schema = @Schema(implementation = AuthResponse.UserInfo.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String username = authentication.getName();
            AuthResponse.UserInfo userInfo = authService.getUserInfoFromToken(
                authentication.getCredentials().toString()
            );
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.warn("Error obteniendo información del usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Cierra la sesión del usuario (logout)
     */
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<Void> logout() {
        log.info("Usuario cerrando sesión");
        
        // En una implementación JWT stateless, el logout se maneja en el cliente
        // Aquí podríamos implementar una lista negra de tokens si fuera necesario
        
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
} 