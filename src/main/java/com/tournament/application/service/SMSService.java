package com.tournament.application.service;

import com.tournament.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio para el envío de SMS
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SMSService {

    /**
     * Envía una notificación por SMS
     * @param user Usuario destinatario
     * @param eventType Tipo de evento
     * @param message Mensaje personalizado
     * @param data Datos adicionales del evento
     */
    public void sendNotification(User user, NotificationService.NotificationEventType eventType, String message, Object data) {
        log.info("Enviando SMS a {}: {}", user.getPhone(), eventType);
        
        // TODO: Implementar integración con servicio de SMS (Twilio, AWS SNS, etc.)
        // Por ahora solo logueamos la acción
        
        String smsMessage = getSMSMessage(user, eventType, message, data);
        
        log.info("SMS enviado - Mensaje: {}, Destinatario: {}", smsMessage, user.getPhone());
    }

    /**
     * Genera el mensaje SMS según el tipo de evento
     * @param user Usuario destinatario
     * @param eventType Tipo de evento
     * @param message Mensaje personalizado
     * @param data Datos adicionales
     * @return Mensaje SMS
     */
    private String getSMSMessage(User user, NotificationService.NotificationEventType eventType, String message, Object data) {
        return String.format("Tournament Management: %s", message);
    }
} 