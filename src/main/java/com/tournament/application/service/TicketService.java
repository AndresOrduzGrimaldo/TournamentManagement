package com.tournament.application.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.tournament.domain.entity.Ticket;
import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.User;
import com.tournament.domain.repository.TicketRepository;
import com.tournament.domain.repository.TournamentRepository;
import com.tournament.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de aplicación para la gestión de tickets
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;

    /**
     * Crea un ticket para un torneo
     * @param userId ID del usuario
     * @param tournamentId ID del torneo
     * @return Ticket creado
     */
    public Ticket createTicket(Long userId, Long tournamentId) {
        log.info("Creando ticket para usuario {} en torneo {}", userId, tournamentId);

        // Validar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Validar torneo
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Torneo no encontrado"));

        // Validar que el torneo esté abierto para registro
        if (!tournament.isRegistrationOpen()) {
            throw new IllegalStateException("El torneo no está abierto para registro");
        }

        // Validar que no esté completo
        if (tournament.isFull()) {
            throw new IllegalStateException("El torneo está completo");
        }

        // Validar que el usuario no tenga ya un ticket para este torneo
        List<Ticket> existingTickets = ticketRepository.findByUserIdAndTournamentId(userId, tournamentId);
        if (!existingTickets.isEmpty()) {
            throw new IllegalStateException("El usuario ya tiene un ticket para este torneo");
        }

        // Generar códigos únicos
        String qrCode = generateQRCode();
        String uniqueCode = generateUniqueCode();

        // Calcular precios
        BigDecimal price = tournament.getIsFree() ? BigDecimal.ZERO : tournament.getPrice();
        BigDecimal serviceFee = tournament.calculateCommission(price);
        BigDecimal totalAmount = price.add(serviceFee);

        // Crear ticket
        Ticket ticket = Ticket.builder()
                .user(user)
                .tournament(tournament)
                .qrCode(qrCode)
                .uniqueCode(uniqueCode)
                .purchaseDate(LocalDateTime.now())
                .price(price)
                .serviceFee(serviceFee)
                .totalAmount(totalAmount)
                .status(Ticket.TicketStatus.ACTIVE)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        // Incrementar contador de participantes
        tournament.incrementParticipants();
        tournamentRepository.save(tournament);

        log.info("Ticket creado exitosamente: {}", savedTicket.getId());
        return savedTicket;
    }

    /**
     * Obtiene un ticket por ID
     * @param id ID del ticket
     * @return Ticket encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    /**
     * Obtiene un ticket por código QR
     * @param qrCode Código QR del ticket
     * @return Ticket encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketByQRCode(String qrCode) {
        return ticketRepository.findByQrCode(qrCode);
    }

    /**
     * Obtiene un ticket por código único
     * @param uniqueCode Código único del ticket
     * @return Ticket encontrado
     */
    @Transactional(readOnly = true)
    public Optional<Ticket> getTicketByUniqueCode(String uniqueCode) {
        return ticketRepository.findByUniqueCode(uniqueCode);
    }

    /**
     * Obtiene tickets por usuario
     * @param userId ID del usuario
     * @return Lista de tickets
     */
    @Transactional(readOnly = true)
    public java.util.List<Ticket> getTicketsByUser(Long userId) {
        return ticketRepository.findByUserId(userId);
    }

    /**
     * Obtiene tickets por torneo
     * @param tournamentId ID del torneo
     * @return Lista de tickets
     */
    @Transactional(readOnly = true)
    public java.util.List<Ticket> getTicketsByTournament(Long tournamentId) {
        return ticketRepository.findByTournamentId(tournamentId);
    }

    /**
     * Obtiene todos los tickets
     * @return Lista de todos los tickets
     */
    @Transactional(readOnly = true)
    public java.util.List<Ticket> getAllTickets() {
        return ticketRepository.findAllWithUserAndTournament();
    }

    /**
     * Valida y usa un ticket
     * @param qrCode Código QR del ticket
     * @return true si el ticket es válido y se usó exitosamente
     */
    public boolean validateAndUseTicket(String qrCode) {
        Optional<Ticket> ticketOpt = ticketRepository.findByQrCode(qrCode);
        
        if (ticketOpt.isEmpty()) {
            log.warn("Ticket no encontrado con QR: {}", qrCode);
            return false;
        }

        Ticket ticket = ticketOpt.get();
        
        if (!ticket.isValid()) {
            log.warn("Ticket no válido: {}", ticket.getId());
            return false;
        }

        ticket.markAsUsed();
        ticketRepository.save(ticket);
        
        log.info("Ticket usado exitosamente: {}", ticket.getId());
        return true;
    }

    /**
     * Cancela un ticket
     * @param ticketId ID del ticket
     */
    public void cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket no encontrado"));

        if (ticket.isUsed()) {
            throw new IllegalStateException("No se puede cancelar un ticket ya usado");
        }

        boolean wasActive = Ticket.TicketStatus.ACTIVE.equals(ticket.getStatus()); // Guardar estado previo
        ticket.cancel();
        ticketRepository.save(ticket);

        // Decrementar contador de participantes si el ticket estaba activo antes de cancelar
        if (wasActive) {
            Tournament tournament = ticket.getTournament();
            tournament.decrementParticipants();
            tournamentRepository.save(tournament);
        }

        log.info("Ticket cancelado: {}", ticketId);
    }

    /**
     * Genera una imagen QR en Base64
     * @param qrCode Código QR
     * @return Imagen QR en Base64
     */
    public String generateQRCodeImage(String qrCode) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCode, BarcodeFormat.QR_CODE, 200, 200);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (WriterException | IOException e) {
            log.error("Error generando imagen QR: {}", e.getMessage());
            throw new RuntimeException("Error generando imagen QR", e);
        }
    }

    /**
     * Genera un código QR único
     * @return Código QR único
     */
    private String generateQRCode() {
        String qrCode;
        do {
            qrCode = "TICKET-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        } while (ticketRepository.existsByQrCode(qrCode));
        
        return qrCode;
    }

    /**
     * Genera un código único
     * @return Código único
     */
    private String generateUniqueCode() {
        String uniqueCode;
        do {
            uniqueCode = "TM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (ticketRepository.existsByUniqueCode(uniqueCode));
        
        return uniqueCode;
    }
} 