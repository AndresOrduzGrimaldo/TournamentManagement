package com.tournament.application.service;

import com.tournament.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio para el envío de emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    /**
     * Envía una notificación por email
     * @param user Usuario destinatario
     * @param eventType Tipo de evento
     * @param message Mensaje personalizado
     * @param data Datos adicionales del evento
     */
    public void sendNotificationEmail(User user, NotificationService.NotificationEventType eventType, String message, Object data) {
        log.info("Enviando email a {}: {}", user.getEmail(), eventType);
        
        // TODO: Implementar integración con servicio de email (SendGrid, AWS SES, etc.)
        // Por ahora solo logueamos la acción
        
        String subject = getEmailSubject(eventType);
        String body = getEmailBody(user, eventType, message, data);
        
        log.info("Email enviado - Asunto: {}, Destinatario: {}", subject, user.getEmail());
    }

    /**
     * Genera el asunto del email según el tipo de evento
     * @param eventType Tipo de evento
     * @return Asunto del email
     */
    private String getEmailSubject(NotificationService.NotificationEventType eventType) {
        return switch (eventType) {
            case TOURNAMENT_START -> "¡Tu torneo ha comenzado!";
            case TOURNAMENT_END -> "Torneo finalizado";
            case TOURNAMENT_UPDATED -> "Torneo actualizado";
            case TOURNAMENT_REMINDER -> "Recordatorio de torneo";
            case TICKET_PURCHASED -> "Ticket comprado exitosamente";
            case TICKET_CANCELLED -> "Ticket cancelado";
            case TICKET_VALIDATED -> "Ticket validado";
            case USER_REGISTERED -> "Bienvenido a Tournament Management";
            case USER_LOGIN -> "Nuevo inicio de sesión";
            case SYSTEM_ALERT -> "Alerta del sistema";
        };
    }

    /**
     * Genera el cuerpo del email
     * @param user Usuario destinatario
     * @param eventType Tipo de evento
     * @param message Mensaje personalizado
     * @param data Datos adicionales
     * @return Cuerpo del email
     */
    private String getEmailBody(User user, NotificationService.NotificationEventType eventType, String message, Object data) {
        return String.format("""
            Hola %s,
            
            %s
            
            Gracias por usar Tournament Management.
            
            Saludos,
            El equipo de Tournament Management
            """, user.getUsername(), message);
    }
} 