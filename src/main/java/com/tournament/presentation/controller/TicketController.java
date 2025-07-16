package com.tournament.presentation.controller;

import com.tournament.application.service.TicketService;
import com.tournament.domain.entity.Ticket;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de tickets
 */
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tickets", description = "API para gestión de tickets de torneos")
public class TicketController {

    private final TicketService ticketService;

    /**
     * Crea un ticket para un torneo
     */
    @PostMapping
    @Operation(summary = "Crear ticket", description = "Crea un ticket para un usuario en un torneo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ticket creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Ticket.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Torneo completo o no disponible")
    })
    public ResponseEntity<Ticket> createTicket(
            @Parameter(description = "ID del usuario") @RequestParam Long userId,
            @Parameter(description = "ID del torneo") @RequestParam Long tournamentId) {
        
        log.info("Solicitud de creación de ticket para usuario {} en torneo {}", userId, tournamentId);
        
        try {
            Ticket ticket = ticketService.createTicket(userId, tournamentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Obtiene un ticket por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener ticket", description = "Obtiene un ticket por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket encontrado",
                    content = @Content(schema = @Schema(implementation = Ticket.class))),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<Ticket> getTicket(
            @Parameter(description = "ID del ticket") @PathVariable Long id) {
        
        return ticketService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene un ticket por código QR
     */
    @GetMapping("/qr/{qrCode}")
    @Operation(summary = "Obtener ticket por QR", description = "Obtiene un ticket por su código QR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket encontrado",
                    content = @Content(schema = @Schema(implementation = Ticket.class))),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<Ticket> getTicketByQR(
            @Parameter(description = "Código QR del ticket") @PathVariable String qrCode) {
        
        return ticketService.getTicketByQRCode(qrCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene tickets por usuario
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Tickets por usuario", description = "Obtiene todos los tickets de un usuario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tickets obtenida",
                    content = @Content(schema = @Schema(implementation = Ticket.class)))
    })
    public ResponseEntity<List<Ticket>> getTicketsByUser(
            @Parameter(description = "ID del usuario") @PathVariable Long userId) {
        
        List<Ticket> tickets = ticketService.getTicketsByUser(userId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Obtiene tickets por torneo
     */
    @GetMapping("/tournament/{tournamentId}")
    @Operation(summary = "Tickets por torneo", description = "Obtiene todos los tickets de un torneo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de tickets obtenida",
                    content = @Content(schema = @Schema(implementation = Ticket.class)))
    })
    public ResponseEntity<List<Ticket>> getTicketsByTournament(
            @Parameter(description = "ID del torneo") @PathVariable Long tournamentId) {
        
        List<Ticket> tickets = ticketService.getTicketsByTournament(tournamentId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Valida y usa un ticket
     */
    @PostMapping("/validate")
    @Operation(summary = "Validar ticket", description = "Valida y usa un ticket por código QR")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket validado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Ticket no válido"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<Map<String, Object>> validateTicket(
            @Parameter(description = "Código QR del ticket") @RequestParam String qrCode) {
        
        log.info("Validando ticket con QR: {}", qrCode);
        
        boolean isValid = ticketService.validateAndUseTicket(qrCode);
        
        if (isValid) {
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Ticket validado exitosamente"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "message", "Ticket no válido o ya usado"
            ));
        }
    }

    /**
     * Cancela un ticket
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar ticket", description = "Cancela un ticket")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket cancelado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado"),
        @ApiResponse(responseCode = "409", description = "Ticket ya usado")
    })
    public ResponseEntity<Void> cancelTicket(
            @Parameter(description = "ID del ticket") @PathVariable Long id) {
        
        try {
            ticketService.cancelTicket(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Genera imagen QR para un ticket
     */
    @GetMapping("/{id}/qr-image")
    @Operation(summary = "Generar imagen QR", description = "Genera una imagen QR en Base64 para un ticket")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Imagen QR generada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Ticket no encontrado")
    })
    public ResponseEntity<Map<String, String>> generateQRImage(
            @Parameter(description = "ID del ticket") @PathVariable Long id) {
        
        return ticketService.getTicketById(id)
                .map(ticket -> {
                    String qrImage = ticketService.generateQRCodeImage(ticket.getQrCode());
                    return ResponseEntity.ok(Map.of(
                        "qrCode", ticket.getQrCode(),
                        "qrImage", "data:image/png;base64," + qrImage
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 