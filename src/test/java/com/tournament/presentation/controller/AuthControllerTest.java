package com.tournament.presentation.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetRegisterInfo() throws Exception {
        mockMvc.perform(get("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Endpoint para registro de usuarios"))
                .andExpect(jsonPath("$.method").value("POST"))
                .andExpect(jsonPath("$.contentType").value("application/json"));
    }

    @Test
    void testGetTestEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Auth controller funcionando correctamente"));
    }
} 