package com.tournament.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Controller;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
}

@RestController
class ApiInfoController {
    
    @GetMapping("/api/info")
    public Map<String, Object> getApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Tournament Management API");
        info.put("version", "1.0.0");
        info.put("description", "API para gestión de torneos y competiciones");
        info.put("status", "running");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("tournaments", "/tournaments");
        endpoints.put("auth", "/auth");
        endpoints.put("tickets", "/tickets");
        endpoints.put("api_info", "/api/info");
        
        info.put("endpoints", endpoints);
        
        Map<String, String> documentation = new HashMap<>();
        documentation.put("general", "Información general de la API");
        documentation.put("tournaments", "Gestión de torneos (crear, listar, actualizar)");
        documentation.put("auth", "Autenticación de usuarios (registro, login)");
        documentation.put("tickets", "Sistema de tickets de soporte");
        
        info.put("documentation", documentation);
        
        return info;
    }
    
    @GetMapping("/api/status")
    public Map<String, Object> getApiStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "OK");
        status.put("timestamp", System.currentTimeMillis());
        status.put("uptime", "running");
        status.put("version", "1.0.0");
        
        Map<String, String> services = new HashMap<>();
        services.put("database", "connected");
        services.put("redis", "connected");
        services.put("rabbitmq", "connected");
        
        status.put("services", services);
        
        return status;
    }
    
    @GetMapping("/api/health")
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("message", "Tournament Management API is running");
        health.put("timestamp", System.currentTimeMillis());
        
        return health;
    }
} 