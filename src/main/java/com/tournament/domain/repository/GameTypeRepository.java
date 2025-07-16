package com.tournament.domain.repository;

import com.tournament.domain.entity.GameType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de dominio para la entidad GameType
 */
@Repository
public interface GameTypeRepository extends JpaRepository<GameType, Long> {

    /**
     * Busca un tipo de juego por su código
     * @param code Código del tipo de juego
     * @return Tipo de juego encontrado
     */
    Optional<GameType> findByCode(String code);

    /**
     * Busca tipos de juego activos
     * @return Lista de tipos de juego activos
     */
    List<GameType> findByIsActiveTrue();

    /**
     * Busca tipos de juego por categoría
     * @param categoryId ID de la categoría
     * @return Lista de tipos de juego
     */
    List<GameType> findByCategoryId(Long categoryId);

    /**
     * Busca tipos de juego activos por categoría
     * @param categoryId ID de la categoría
     * @return Lista de tipos de juego activos
     */
    List<GameType> findByCategoryIdAndIsActiveTrue(Long categoryId);

    /**
     * Verifica si existe un tipo de juego con el código dado
     * @param code Código del tipo de juego
     * @return true si existe
     */
    boolean existsByCode(String code);

    /**
     * Busca tipos de juego por número de jugadores
     * @param playersCount Número de jugadores
     * @return Lista de tipos de juego
     */
    List<GameType> findByPlayersCount(Integer playersCount);

    /**
     * Busca tipos de juego de equipo (más de 1 jugador)
     * @return Lista de tipos de juego de equipo
     */
    List<GameType> findByPlayersCountGreaterThan(Integer playersCount);

    /**
     * Busca tipos de juego individuales (1 jugador)
     * @return Lista de tipos de juego individuales
     */
    List<GameType> findByPlayersCountEquals(Integer playersCount);
} 