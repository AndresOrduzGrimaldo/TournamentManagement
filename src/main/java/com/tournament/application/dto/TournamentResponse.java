package com.tournament.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la respuesta de torneos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TournamentResponse {

    private Long id;
    private String name;
    private String description;
    private CategoryResponse category;
    private GameTypeResponse gameType;
    private UserResponse organizer;
    private Boolean isFree;
    private BigDecimal price;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private BigDecimal commissionPercentage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * DTO para la respuesta de categorías
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResponse {
        private Long id;
        private String code;
        private String description;
        private String alias;
    }

    /**
     * DTO para la respuesta de tipos de juego
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameTypeResponse {
        private Long id;
        private String code;
        private String fullName;
        private Integer playersCount;
        private CategoryResponse category;
    }

    /**
     * DTO para la respuesta de usuarios
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private String fullName;
    }

    /**
     * Calcula las plazas disponibles
     * @return Número de plazas disponibles
     */
    public Integer getAvailableSlots() {
        if (maxParticipants == null || currentParticipants == null) {
            return 0;
        }
        return Math.max(0, maxParticipants - currentParticipants);
    }

    /**
     * Verifica si el torneo está completo
     * @return true si está completo
     */
    public Boolean isFull() {
        return currentParticipants != null && 
               maxParticipants != null && 
               currentParticipants >= maxParticipants;
    }

    /**
     * Verifica si el torneo está abierto para registro
     * @return true si está abierto
     */
    public Boolean isRegistrationOpen() {
        return "REGISTRATION_OPEN".equals(status) && 
               !isFull() && 
               startDate != null && 
               startDate.isAfter(LocalDateTime.now());
    }
} 