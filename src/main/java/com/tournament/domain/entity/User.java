package com.tournament.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entidad de dominio que representa un usuario del sistema
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Column(name = "is_active")
    private Boolean isActive;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Enum que define los roles de usuario en el sistema
     */
    public enum UserRole {
        ADMIN,
        SUBADMIN,
        PARTICIPANT
    }

    /**
     * Valida si el usuario puede crear un torneo gratuito
     * @param currentFreeTournaments Número actual de torneos gratuitos del usuario
     * @param maxFreeTournaments Límite máximo de torneos gratuitos
     * @return true si puede crear el torneo
     */
    public boolean canCreateFreeTournament(int currentFreeTournaments, int maxFreeTournaments) {
        return this.isActive && 
               (this.role == UserRole.ADMIN || this.role == UserRole.SUBADMIN) &&
               currentFreeTournaments < maxFreeTournaments;
    }

    /**
     * Obtiene el nombre completo del usuario
     * @return Nombre completo
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Verifica si el usuario tiene permisos de administrador
     * @return true si es admin
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    /**
     * Verifica si el usuario tiene permisos de subadministrador
     * @return true si es subadmin
     */
    public boolean isSubAdmin() {
        return UserRole.SUBADMIN.equals(this.role);
    }
} 