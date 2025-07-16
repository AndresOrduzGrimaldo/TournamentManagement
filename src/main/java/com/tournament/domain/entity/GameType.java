package com.tournament.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un tipo de juego
 */
@Entity
@Table(name = "game_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class GameType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 10)
    private String code;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "players_count", nullable = false)
    private Integer playersCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Verifica si el tipo de juego está activo
     * @return true si está activo
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    /**
     * Verifica si es un juego de equipo
     * @return true si es de equipo
     */
    public boolean isTeamGame() {
        return this.playersCount > 1;
    }

    /**
     * Verifica si es un juego individual
     * @return true si es individual
     */
    public boolean isIndividualGame() {
        return this.playersCount == 1;
    }
} 