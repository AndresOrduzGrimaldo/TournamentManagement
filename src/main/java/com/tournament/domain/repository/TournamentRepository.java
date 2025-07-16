package com.tournament.domain.repository;

import com.tournament.domain.entity.Tournament;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de dominio para la entidad Tournament
 */
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    /**
     * Busca torneos por organizador
     * @param organizerId ID del organizador
     * @return Lista de torneos
     */
    List<Tournament> findByOrganizerId(Long organizerId);

    /**
     * Busca torneos por categoría
     * @param categoryId ID de la categoría
     * @return Lista de torneos
     */
    List<Tournament> findByCategoryId(Long categoryId);

    /**
     * Busca torneos por estado
     * @param status Estado del torneo
     * @return Lista de torneos
     */
    List<Tournament> findByStatus(Tournament.TournamentStatus status);

    /**
     * Busca torneos gratuitos
     * @return Lista de torneos gratuitos
     */
    List<Tournament> findByIsFreeTrue();

    /**
     * Busca torneos pagados
     * @return Lista de torneos pagados
     */
    List<Tournament> findByIsFreeFalse();

    /**
     * Busca torneos que están abiertos para registro
     * @return Lista de torneos abiertos
     */
    @Query("SELECT t FROM Tournament t WHERE t.status = 'REGISTRATION_OPEN' AND t.currentParticipants < t.maxParticipants AND t.startDate > :now")
    List<Tournament> findOpenForRegistration(@Param("now") LocalDateTime now);

    /**
     * Busca torneos en progreso
     * @param now Fecha actual
     * @return Lista de torneos en progreso
     */
    @Query("SELECT t FROM Tournament t WHERE t.status = 'IN_PROGRESS' AND t.startDate <= :now AND t.endDate >= :now")
    List<Tournament> findInProgress(@Param("now") LocalDateTime now);

    /**
     * Busca torneos por rango de fechas
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de torneos
     */
    @Query("SELECT t FROM Tournament t WHERE t.startDate >= :startDate AND t.endDate <= :endDate")
    List<Tournament> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Busca torneos con paginación
     * @param pageable Configuración de paginación
     * @return Página de torneos
     */
    Page<Tournament> findAll(Pageable pageable);

    /**
     * Busca torneos por organizador con paginación
     * @param organizerId ID del organizador
     * @param pageable Configuración de paginación
     * @return Página de torneos
     */
    Page<Tournament> findByOrganizerId(Long organizerId, Pageable pageable);

    /**
     * Cuenta torneos gratuitos por organizador
     * @param organizerId ID del organizador
     * @return Número de torneos gratuitos
     */
    @Query("SELECT COUNT(t) FROM Tournament t WHERE t.organizer.id = :organizerId AND t.isFree = true")
    int countFreeTournamentsByOrganizer(@Param("organizerId") Long organizerId);

    /**
     * Busca torneos que necesitan actualización de estado
     * @param now Fecha actual
     * @return Lista de torneos que necesitan actualización
     */
    @Query("SELECT t FROM Tournament t WHERE " +
           "(t.status = 'REGISTRATION_OPEN' AND t.startDate <= :now) OR " +
           "(t.status = 'IN_PROGRESS' AND t.endDate <= :now)")
    List<Tournament> findTournamentsNeedingStatusUpdate(@Param("now") LocalDateTime now);

    /**
     * Busca torneos por nombre (búsqueda parcial)
     * @param name Nombre del torneo
     * @return Lista de torneos
     */
    @Query("SELECT t FROM Tournament t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Tournament> findByNameContainingIgnoreCase(@Param("name") String name);
} 