package com.tournament.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tournament.domain.entity.Ticket;

/**
 * Repositorio de dominio para la entidad Ticket
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * Busca tickets por usuario
     * @param userId ID del usuario
     * @return Lista de tickets
     */
    List<Ticket> findByUserId(Long userId);

    /**
     * Busca tickets por torneo
     * @param tournamentId ID del torneo
     * @return Lista de tickets
     */
    List<Ticket> findByTournamentId(Long tournamentId);

    /**
     * Busca tickets por estado
     * @param status Estado del ticket
     * @return Lista de tickets
     */
    List<Ticket> findByStatus(Ticket.TicketStatus status);

    /**
     * Busca un ticket por su código QR
     * @param qrCode Código QR del ticket
     * @return Ticket encontrado
     */
    Optional<Ticket> findByQrCode(String qrCode);

    /**
     * Busca un ticket por su código único
     * @param uniqueCode Código único del ticket
     * @return Ticket encontrado
     */
    Optional<Ticket> findByUniqueCode(String uniqueCode);

    /**
     * Busca tickets activos por torneo
     * @param tournamentId ID del torneo
     * @return Lista de tickets activos
     */
    List<Ticket> findByTournamentIdAndStatus(Long tournamentId, Ticket.TicketStatus status);

    /**
     * Cuenta tickets por torneo
     * @param tournamentId ID del torneo
     * @return Número de tickets
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.tournament.id = :tournamentId")
    int countByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Cuenta tickets activos por torneo
     * @param tournamentId ID del torneo
     * @return Número de tickets activos
     */
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.tournament.id = :tournamentId AND t.status = 'ACTIVE'")
    int countActiveByTournamentId(@Param("tournamentId") Long tournamentId);



    /**
     * Busca tickets por rango de fechas de compra
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Lista de tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.purchaseDate >= :startDate AND t.purchaseDate <= :endDate")
    List<Ticket> findByPurchaseDateRange(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Busca tickets válidos para un torneo
     * @param tournamentId ID del torneo
     * @return Lista de tickets válidos
     */
    @Query("SELECT t FROM Ticket t WHERE t.tournament.id = :tournamentId AND t.status = 'ACTIVE' AND t.usedAt IS NULL")
    List<Ticket> findValidTicketsByTournamentId(@Param("tournamentId") Long tournamentId);

    /**
     * Verifica si existe un ticket con el código QR dado
     * @param qrCode Código QR del ticket
     * @return true si existe
     */
    boolean existsByQrCode(String qrCode);

    /**
     * Verifica si existe un ticket con el código único dado
     * @param uniqueCode Código único del ticket
     * @return true si existe
     */
    boolean existsByUniqueCode(String uniqueCode);

    /**
     * Obtiene todos los tickets con usuario y torneo cargados
     * @return Lista de tickets con entidades relacionadas
     */
    @Query("SELECT t FROM Ticket t " +
           "JOIN FETCH t.user " +
           "JOIN FETCH t.tournament tr " +
           "JOIN FETCH tr.category " +
           "JOIN FETCH tr.gameType gt " +
           "JOIN FETCH gt.category")
    List<Ticket> findAllWithUserAndTournament();

    /**
     * Busca tickets por usuario y torneo
     * @param userId ID del usuario
     * @param tournamentId ID del torneo
     * @return Lista de tickets
     */
    List<Ticket> findByUserIdAndTournamentId(Long userId, Long tournamentId);
} 