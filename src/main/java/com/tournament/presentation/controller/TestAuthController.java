package com.tournament.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class TestAuthController {

    @GetMapping("/register")
    public ResponseEntity<Map<String, Object>> getRegisterInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("message", "Endpoint para registro de usuarios");
        info.put("method", "POST");
        info.put("contentType", "application/json");
        info.put("requiredFields", Map.of(
            "username", "String (requerido)",
            "email", "String (requerido, formato email)",
            "password", "String (requerido, mínimo 6 caracteres)"
        ));
        info.put("example", Map.of(
            "username", "usuario123",
            "email", "usuario@ejemplo.com",
            "password", "password123"
        ));
        return ResponseEntity.ok(info);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth controller funcionando correctamente");
    }
} 