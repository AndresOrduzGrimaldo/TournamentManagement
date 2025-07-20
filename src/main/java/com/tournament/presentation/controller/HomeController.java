package com.tournament.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tournament Management API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("endpoints", Map.of(
            "tournaments", "/api/tournaments",
            "auth", "/api/auth",
            "tickets", "/api/tickets"
        ));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Tournament Management API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("endpoints", Map.of(
            "tournaments", "/api/tournaments",
            "auth", "/api/auth",
            "tickets", "/api/tickets"
        ));
        return ResponseEntity.ok(response);
    }
} 