package com.tournament.application.service;

import com.tournament.application.dto.CreateTournamentRequest;
import com.tournament.application.dto.TournamentResponse;
import com.tournament.domain.entity.*;
import com.tournament.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación para la gestión de torneos
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final GameTypeRepository gameTypeRepository;

    /**
     * Crea un nuevo torneo
     * @param request Solicitud de creación
     * @return Torneo creado
     */
    public TournamentResponse createTournament(CreateTournamentRequest request) {
        log.info("Creando torneo: {}", request.getName());

        // Validar organizador
        User organizer = userRepository.findById(request.getOrganizerId())
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));

        // Validar categoría
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Validar tipo de juego
        GameType gameType = gameTypeRepository.findById(request.getGameTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de juego no encontrado"));

        // Validar límites de torneos gratuitos
        if (Boolean.TRUE.equals(request.getIsFree())) {
            validateFreeTournamentLimits(organizer);
        }

        // Crear torneo
        Tournament tournament = Tournament.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(category)
                .gameType(gameType)
                .organizer(organizer)
                .isFree(request.getIsFree())
                .price(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO)
                .maxParticipants(request.getMaxParticipants())
                .currentParticipants(0)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(Tournament.TournamentStatus.DRAFT)
                .commissionPercentage(request.getCommissionPercentage() != null ? 
                                   request.getCommissionPercentage() : BigDecimal.valueOf(5.0))
                .build();

        Tournament savedTournament = tournamentRepository.save(tournament);
        log.info("Torneo creado exitosamente: {}", savedTournament.getId());

        return mapToResponse(savedTournament);
    }

    /**
     * Obtiene un torneo por ID
     * @param id ID del torneo
     * @return Torneo encontrado
     */
    @Transactional(readOnly = true)
    public Optional<TournamentResponse> getTournamentById(Long id) {
        return tournamentRepository.findById(id)
                .map(this::mapToResponse);
    }

    /**
     * Obtiene todos los torneos
     * @return Lista de torneos
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getAllTournaments() {
        return tournamentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene torneos por organizador
     * @param organizerId ID del organizador
     * @return Lista de torneos
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getTournamentsByOrganizer(Long organizerId) {
        return tournamentRepository.findByOrganizerId(organizerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene torneos abiertos para registro
     * @return Lista de torneos abiertos
     */
    @Transactional(readOnly = true)
    public List<TournamentResponse> getOpenTournaments() {
        return tournamentRepository.findOpenForRegistration(LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza el estado de un torneo
     * @param id ID del torneo
     * @param status Nuevo estado
     * @return Torneo actualizado
     */
    public TournamentResponse updateTournamentStatus(Long id, Tournament.TournamentStatus status) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        tournament.setStatus(status);
        Tournament updatedTournament = tournamentRepository.save(tournament);

        log.info("Estado del torneo {} actualizado a: {}", id, status);
        return mapToResponse(updatedTournament);
    }

    /**
     * Incrementa el contador de participantes
     * @param tournamentId ID del torneo
     */
    public void incrementParticipants(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        if (tournament.isFull()) {
            throw new IllegalStateException("El torneo está completo");
        }

        tournament.incrementParticipants();
        tournamentRepository.save(tournament);
        log.info("Participante agregado al torneo: {}", tournamentId);
    }

    /**
     * Decrementa el contador de participantes
     * @param tournamentId ID del torneo
     */
    public void decrementParticipants(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        tournament.decrementParticipants();
        tournamentRepository.save(tournament);
        log.info("Participante removido del torneo: {}", tournamentId);
    }

    /**
     * Valida los límites de torneos gratuitos
     * @param organizer Organizador del torneo
     */
    private void validateFreeTournamentLimits(User organizer) {
        int currentFreeTournaments = tournamentRepository.countFreeTournamentsByOrganizer(organizer.getId());
        int maxFreeTournaments = 2; // Configurable

        if (currentFreeTournaments >= maxFreeTournaments) {
            throw new IllegalStateException(
                String.format("El organizador ya tiene %d torneos gratuitos. Límite máximo: %d", 
                           currentFreeTournaments, maxFreeTournaments));
        }
    }

    /**
     * Mapea una entidad Tournament a TournamentResponse
     * @param tournament Entidad del torneo
     * @return DTO de respuesta
     */
    private TournamentResponse mapToResponse(Tournament tournament) {
        return TournamentResponse.builder()
                .id(tournament.getId())
                .name(tournament.getName())
                .description(tournament.getDescription())
                .category(mapCategoryResponse(tournament.getCategory()))
                .gameType(mapGameTypeResponse(tournament.getGameType()))
                .organizer(mapUserResponse(tournament.getOrganizer()))
                .isFree(tournament.getIsFree())
                .price(tournament.getPrice())
                .maxParticipants(tournament.getMaxParticipants())
                .currentParticipants(tournament.getCurrentParticipants())
                .startDate(tournament.getStartDate())
                .endDate(tournament.getEndDate())
                .status(tournament.getStatus().name())
                .commissionPercentage(tournament.getCommissionPercentage())
                .createdAt(tournament.getCreatedAt())
                .updatedAt(tournament.getUpdatedAt())
                .build();
    }

    /**
     * Mapea una entidad Category a CategoryResponse
     */
    private TournamentResponse.CategoryResponse mapCategoryResponse(Category category) {
        if (category == null) return null;
        
        return TournamentResponse.CategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .description(category.getDescription())
                .alias(category.getAlias())
                .build();
    }

    /**
     * Mapea una entidad GameType a GameTypeResponse
     */
    private TournamentResponse.GameTypeResponse mapGameTypeResponse(GameType gameType) {
        if (gameType == null) return null;
        
        return TournamentResponse.GameTypeResponse.builder()
                .id(gameType.getId())
                .code(gameType.getCode())
                .fullName(gameType.getFullName())
                .playersCount(gameType.getPlayersCount())
                .category(mapCategoryResponse(gameType.getCategory()))
                .build();
    }

    /**
     * Mapea una entidad User a UserResponse
     */
    private TournamentResponse.UserResponse mapUserResponse(User user) {
        if (user == null) return null;
        
        return TournamentResponse.UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .build();
    }
} 