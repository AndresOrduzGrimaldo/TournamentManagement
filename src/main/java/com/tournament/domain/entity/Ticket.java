package com.tournament.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un ticket de acceso a un torneo
 */
@Entity
@Table(name = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;

    @Column(name = "qr_code", unique = true, nullable = false)
    private String qrCode;

    @Column(name = "unique_code", unique = true, nullable = false, length = 50)
    private String uniqueCode;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "service_fee", precision = 10, scale = 2, nullable = false)
    private BigDecimal serviceFee;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TicketStatus status;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enum que define los estados posibles de un ticket
     */
    public enum TicketStatus {
        ACTIVE,
        USED,
        CANCELLED,
        EXPIRED
    }

    /**
     * Verifica si el ticket está activo
     * @return true si está activo
     */
    public boolean isActive() {
        return TicketStatus.ACTIVE.equals(this.status);
    }

    /**
     * Verifica si el ticket ha sido usado
     * @return true si ha sido usado
     */
    public boolean isUsed() {
        return TicketStatus.USED.equals(this.status) && this.usedAt != null;
    }

    /**
     * Marca el ticket como usado
     */
    public void markAsUsed() {
        this.status = TicketStatus.USED;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Cancela el ticket
     */
    public void cancel() {
        this.status = TicketStatus.CANCELLED;
    }

    /**
     * Expira el ticket
     */
    public void expire() {
        this.status = TicketStatus.EXPIRED;
    }

    /**
     * Calcula el total del ticket incluyendo comisión
     * @return Total calculado
     */
    public BigDecimal calculateTotal() {
        return this.price.add(this.serviceFee);
    }

    /**
     * Verifica si el ticket es válido para usar
     * @return true si es válido
     */
    public boolean isValid() {
        return isActive() && !isUsed() && 
               this.tournament != null && 
               this.tournament.isInProgress();
    }
} 