package com.tournament.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador de prueba para verificar el funcionamiento de la aplicación
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/health")
    public String health() {
        return "OK - Tournament Management API is running!";
    }

    @GetMapping("/info")
    public String info() {
        return "Tournament Management API v1.0.0";
    }
} 