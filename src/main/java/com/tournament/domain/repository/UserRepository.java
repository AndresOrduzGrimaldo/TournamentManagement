package com.tournament.domain.repository;

import com.tournament.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de dominio para la entidad User
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario
     * @param username Nombre de usuario
     * @return Usuario encontrado
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su email
     * @param email Email del usuario
     * @return Usuario encontrado
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca usuarios activos por rol
     * @param role Rol del usuario
     * @return Lista de usuarios activos
     */
    List<User> findByRoleAndIsActiveTrue(User.UserRole role);

    /**
     * Verifica si existe un usuario con el nombre de usuario dado
     * @param username Nombre de usuario
     * @return true si existe
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email dado
     * @param email Email del usuario
     * @return true si existe
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios que pueden crear torneos gratuitos
     * @param maxFreeTournaments Límite máximo de torneos gratuitos
     * @return Lista de usuarios elegibles
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND (u.role = 'ADMIN' OR u.role = 'SUBADMIN')")
    List<User> findUsersEligibleForFreeTournaments();

    /**
     * Cuenta los torneos gratuitos creados por un usuario
     * @param userId ID del usuario
     * @return Número de torneos gratuitos
     */
    @Query("SELECT COUNT(t) FROM Tournament t WHERE t.organizer.id = :userId AND t.isFree = true")
    int countFreeTournamentsByUser(@Param("userId") Long userId);
} 