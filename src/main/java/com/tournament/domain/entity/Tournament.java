package com.tournament.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un torneo de videojuegos
 */
@Entity
@Table(name = "tournaments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_type_id")
    private GameType gameType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @Column(name = "is_free", nullable = false)
    private Boolean isFree;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "current_participants")
    private Integer currentParticipants;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TournamentStatus status;

    @Column(name = "commission_percentage", precision = 5, scale = 2)
    private BigDecimal commissionPercentage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum que define los estados posibles de un torneo
     */
    public enum TournamentStatus {
        DRAFT,
        PUBLISHED,
        REGISTRATION_OPEN,
        REGISTRATION_CLOSED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    /**
     * Verifica si el torneo está abierto para registros
     * @return true si está abierto
     */
    public boolean isRegistrationOpen() {
        return TournamentStatus.REGISTRATION_OPEN.equals(this.status) &&
               this.currentParticipants < this.maxParticipants &&
               LocalDateTime.now().isBefore(this.startDate);
    }

    /**
     * Verifica si el torneo está en progreso
     * @return true si está en progreso
     */
    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return TournamentStatus.IN_PROGRESS.equals(this.status) &&
               now.isAfter(this.startDate) && now.isBefore(this.endDate);
    }

    /**
     * Calcula el número de plazas disponibles
     * @return Plazas disponibles
     */
    public int getAvailableSlots() {
        return this.maxParticipants - this.currentParticipants;
    }

    /**
     * Verifica si el torneo está completo
     * @return true si está completo
     */
    public boolean isFull() {
        return this.currentParticipants >= this.maxParticipants;
    }

    /**
     * Incrementa el contador de participantes
     */
    public void incrementParticipants() {
        if (this.currentParticipants < this.maxParticipants) {
            this.currentParticipants++;
        }
    }

    /**
     * Decrementa el contador de participantes
     */
    public void decrementParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    /**
     * Calcula la comisión por ticket
     * @param ticketPrice Precio del ticket
     * @return Comisión calculada
     */
    public BigDecimal calculateCommission(BigDecimal ticketPrice) {
        if (this.commissionPercentage == null || ticketPrice == null) {
            return BigDecimal.ZERO;
        }
        return ticketPrice.multiply(this.commissionPercentage)
                         .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
    }
} 