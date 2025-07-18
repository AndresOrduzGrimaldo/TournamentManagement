package com.tournament.application.service;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio de aplicación para la gestión de notificaciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final EmailService emailService;
    private final SMSService smsService;
    private final PushNotificationService pushNotificationService;
    private final WebSocketService webSocketService;

    /**
     * Notifica a un usuario sobre un evento específico
     * @param user Usuario a notificar
     * @param eventType Tipo de evento
     * @param message Mensaje personalizado
     * @param data Datos adicionales del evento
     */
    public void notifyUser(User user, NotificationEventType eventType, String message, Object data) {
        log.info("Enviando notificación a usuario {}: {}", user.getUsername(), eventType);

        try {
            // Enviar email
            emailService.sendNotificationEmail(user, eventType, message, data);

            // Enviar notificación push si está habilitada
            if (user.isPushNotificationsEnabled()) {
                pushNotificationService.sendNotification(user, eventType, message, data);
            }

            // Enviar SMS si está habilitado
            if (user.isSmsNotificationsEnabled()) {
                smsService.sendNotification(user, eventType, message, data);
            }

            // Enviar notificación en tiempo real
            webSocketService.sendNotification(user.getId(), eventType, message, data);

            log.info("Notificación enviada exitosamente a usuario {}", user.getUsername());
        } catch (Exception e) {
            log.error("Error enviando notificación a usuario {}: {}", user.getUsername(), e.getMessage());
        }
    }

    /**
     * Notifica a todos los participantes de un torneo
     * @param tournament Torneo
     * @param eventType Tipo de evento
     * @param message Mensaje personalizado
     * @param data Datos adicionales del evento
     */
    public void notifyTournamentParticipants(Tournament tournament, NotificationEventType eventType, String message, Object data) {
        log.info("Notificando a participantes del torneo {}: {}", tournament.getName(), eventType);

        // Obtener participantes del torneo
        List<User> participants = getTournamentParticipants(tournament);

        for (User participant : participants) {
            notifyUser(participant, eventType, message, data);
        }

        log.info("Notificaciones enviadas a {} participantes del torneo {}", participants.size(), tournament.getName());
    }

    /**
     * Notifica sobre el inicio de un torneo
     * @param tournament Torneo que está iniciando
     */
    public void notifyTournamentStart(Tournament tournament) {
        String message = String.format("¡El torneo '%s' ha comenzado! Prepárate para competir.", tournament.getName());
        
        notifyTournamentParticipants(tournament, NotificationEventType.TOURNAMENT_START, message, tournament);
        
        // Notificar al organizador
        notifyUser(tournament.getOrganizer(), NotificationEventType.TOURNAMENT_START, 
                  "Tu torneo ha comenzado exitosamente", tournament);
    }

    /**
     * Notifica sobre el fin de un torneo
     * @param tournament Torneo que ha terminado
     */
    public void notifyTournamentEnd(Tournament tournament) {
        String message = String.format("¡El torneo '%s' ha terminado! Gracias por participar.", tournament.getName());
        
        notifyTournamentParticipants(tournament, NotificationEventType.TOURNAMENT_END, message, tournament);
        
        // Notificar al organizador
        notifyUser(tournament.getOrganizer(), NotificationEventType.TOURNAMENT_END, 
                  "Tu torneo ha terminado", tournament);
    }

    /**
     * Notifica sobre la compra de un ticket
     * @param user Usuario que compró el ticket
     * @param tournament Torneo del ticket
     * @param ticketId ID del ticket
     */
    public void notifyTicketPurchase(User user, Tournament tournament, Long ticketId) {
        String message = String.format("¡Ticket comprado exitosamente para el torneo '%s'!", tournament.getName());
        
        notifyUser(user, NotificationEventType.TICKET_PURCHASED, message, 
                  Map.of("tournamentId", tournament.getId(), "ticketId", ticketId));
    }

    /**
     * Notifica sobre la cancelación de un ticket
     * @param user Usuario que canceló el ticket
     * @param tournament Torneo del ticket
     * @param ticketId ID del ticket
     */
    public void notifyTicketCancellation(User user, Tournament tournament, Long ticketId) {
        String message = String.format("Ticket cancelado para el torneo '%s'.", tournament.getName());
        
        notifyUser(user, NotificationEventType.TICKET_CANCELLED, message, 
                  Map.of("tournamentId", tournament.getId(), "ticketId", ticketId));
    }

    /**
     * Notifica sobre cambios en el torneo
     * @param tournament Torneo modificado
     * @param changeType Tipo de cambio
     */
    public void notifyTournamentChange(Tournament tournament, String changeType) {
        String message = String.format("El torneo '%s' ha sido actualizado: %s", tournament.getName(), changeType);
        
        notifyTournamentParticipants(tournament, NotificationEventType.TOURNAMENT_UPDATED, message, tournament);
    }

    /**
     * Notifica sobre recordatorios de torneo
     * @param tournament Torneo
     * @param hoursUntilStart Horas hasta el inicio
     */
    public void notifyTournamentReminder(Tournament tournament, int hoursUntilStart) {
        String message = String.format("Recordatorio: El torneo '%s' comienza en %d horas", 
                                     tournament.getName(), hoursUntilStart);
        
        notifyTournamentParticipants(tournament, NotificationEventType.TOURNAMENT_REMINDER, message, tournament);
    }

    /**
     * Obtiene los participantes de un torneo
     * @param tournament Torneo
     * @return Lista de participantes
     */
    private List<User> getTournamentParticipants(Tournament tournament) {
        // TODO: Implementar lógica para obtener participantes del torneo
        // Por ahora retornamos una lista vacía
        return List.of();
    }

    /**
     * Enum que define los tipos de eventos de notificación
     */
    public enum NotificationEventType {
        TOURNAMENT_START,
        TOURNAMENT_END,
        TOURNAMENT_UPDATED,
        TOURNAMENT_REMINDER,
        TICKET_PURCHASED,
        TICKET_CANCELLED,
        TICKET_VALIDATED,
        USER_REGISTERED,
        USER_LOGIN,
        SYSTEM_ALERT
    }
} 