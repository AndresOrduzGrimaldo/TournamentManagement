package com.tournament.presentation.controller;

import com.tournament.application.dto.CreateTournamentRequest;
import com.tournament.application.dto.TournamentResponse;
import com.tournament.application.service.TournamentService;
import com.tournament.domain.entity.Tournament;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para la gestión de torneos
 */
@RestController
@RequestMapping("/tournaments")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Torneos", description = "API para gestión de torneos de videojuegos")
public class TournamentController {

    private final TournamentService tournamentService;

    /**
     * Crea un nuevo torneo
     */
    @PostMapping
    @Operation(summary = "Crear torneo", description = "Crea un nuevo torneo de videojuegos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Torneo creado exitosamente",
                    content = @Content(schema = @Schema(implementation = TournamentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Conflicto con límites de torneos gratuitos")
    })
    public ResponseEntity<TournamentResponse> createTournament(
            @Valid @RequestBody CreateTournamentRequest request) {
        
        log.info("Solicitud de creación de torneo: {}", request.getName());
        
        TournamentResponse tournament = tournamentService.createTournament(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(tournament);
    }

    /**
     * Obtiene un torneo por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener torneo", description = "Obtiene un torneo por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Torneo encontrado",
                    content = @Content(schema = @Schema(implementation = TournamentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    public ResponseEntity<TournamentResponse> getTournament(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        return tournamentService.getTournamentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene todos los torneos
     */
    @GetMapping
    @Operation(summary = "Listar torneos", description = "Obtiene todos los torneos disponibles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de torneos obtenida",
                    content = @Content(schema = @Schema(implementation = TournamentResponse.class)))
    })
    public ResponseEntity<List<TournamentResponse>> getAllTournaments() {
        List<TournamentResponse> tournaments = tournamentService.getAllTournaments();
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Obtiene torneos por organizador
     */
    @GetMapping("/organizer/{organizerId}")
    @Operation(summary = "Torneos por organizador", description = "Obtiene todos los torneos de un organizador")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de torneos obtenida",
                    content = @Content(schema = @Schema(implementation = TournamentResponse.class)))
    })
    public ResponseEntity<List<TournamentResponse>> getTournamentsByOrganizer(
            @Parameter(description = "ID del organizador") @PathVariable Long organizerId) {
        
        List<TournamentResponse> tournaments = tournamentService.getTournamentsByOrganizer(organizerId);
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Obtiene torneos abiertos para registro
     */
    @GetMapping("/open")
    @Operation(summary = "Torneos abiertos", description = "Obtiene torneos abiertos para registro")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de torneos abiertos",
                    content = @Content(schema = @Schema(implementation = TournamentResponse.class)))
    })
    public ResponseEntity<List<TournamentResponse>> getOpenTournaments() {
        List<TournamentResponse> tournaments = tournamentService.getOpenTournaments();
        return ResponseEntity.ok(tournaments);
    }

    /**
     * Actualiza el estado de un torneo
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Actualizar estado", description = "Actualiza el estado de un torneo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = TournamentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    public ResponseEntity<TournamentResponse> updateTournamentStatus(
            @Parameter(description = "ID del torneo") @PathVariable Long id,
            @Parameter(description = "Nuevo estado del torneo") @RequestParam Tournament.TournamentStatus status) {
        
        try {
            TournamentResponse tournament = tournamentService.updateTournamentStatus(id, status);
            return ResponseEntity.ok(tournament);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Incrementa el contador de participantes
     */
    @PostMapping("/{id}/participants/increment")
    @Operation(summary = "Agregar participante", description = "Incrementa el contador de participantes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Participante agregado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado"),
        @ApiResponse(responseCode = "409", description = "Torneo completo")
    })
    public ResponseEntity<Void> incrementParticipants(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        try {
            tournamentService.incrementParticipants(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Decrementa el contador de participantes
     */
    @PostMapping("/{id}/participants/decrement")
    @Operation(summary = "Remover participante", description = "Decrementa el contador de participantes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Participante removido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Torneo no encontrado")
    })
    public ResponseEntity<Void> decrementParticipants(
            @Parameter(description = "ID del torneo") @PathVariable Long id) {
        
        try {
            tournamentService.decrementParticipants(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 