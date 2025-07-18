package com.tournament.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para el envío de notificaciones en tiempo real via WebSocket
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una notificación en tiempo real a un usuario específico
     * @param userId ID del usuario destinatario
     * @param eventType Tipo de evento
     * @param message Mensaje personalizado
     * @param data Datos adicionales del evento
     */
    public void sendNotification(Long userId, NotificationService.NotificationEventType eventType, String message, Object data) {
        log.info("Enviando notificación WebSocket a usuario {}: {}", userId, eventType);
        
        try {
            String destination = "/user/" + userId + "/notifications";
            
            NotificationPayload payload = NotificationPayload.builder()
                    .eventType(eventType)
                    .message(message)
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("Notificación WebSocket enviada exitosamente a usuario {}", userId);
        } catch (Exception e) {
            log.error("Error enviando notificación WebSocket a usuario {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Envía una notificación a todos los usuarios conectados
     * @param eventType Tipo de evento
     * @param message Mensaje personalizado
     * @param data Datos adicionales del evento
     */
    public void broadcastNotification(NotificationService.NotificationEventType eventType, String message, Object data) {
        log.info("Enviando notificación broadcast: {}", eventType);
        
        try {
            String destination = "/topic/notifications";
            
            NotificationPayload payload = NotificationPayload.builder()
                    .eventType(eventType)
                    .message(message)
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("Notificación broadcast enviada exitosamente");
        } catch (Exception e) {
            log.error("Error enviando notificación broadcast: {}", e.getMessage());
        }
    }

    /**
     * Envía una notificación a un canal específico
     * @param channel Canal de destino
     * @param eventType Tipo de evento
     * @param message Mensaje personalizado
     * @param data Datos adicionales del evento
     */
    public void sendToChannel(String channel, NotificationService.NotificationEventType eventType, String message, Object data) {
        log.info("Enviando notificación al canal {}: {}", channel, eventType);
        
        try {
            String destination = "/topic/" + channel;
            
            NotificationPayload payload = NotificationPayload.builder()
                    .eventType(eventType)
                    .message(message)
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("Notificación enviada exitosamente al canal {}", channel);
        } catch (Exception e) {
            log.error("Error enviando notificación al canal {}: {}", channel, e.getMessage());
        }
    }

    /**
     * Clase interna para el payload de notificaciones
     */
    public static class NotificationPayload {
        private NotificationService.NotificationEventType eventType;
        private String message;
        private Object data;
        private long timestamp;

        // Constructor, getters y setters
        public NotificationPayload() {}

        public NotificationService.NotificationEventType getEventType() {
            return eventType;
        }

        public void setEventType(NotificationService.NotificationEventType eventType) {
            this.eventType = eventType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private NotificationPayload payload = new NotificationPayload();

            public Builder eventType(NotificationService.NotificationEventType eventType) {
                payload.eventType = eventType;
                return this;
            }

            public Builder message(String message) {
                payload.message = message;
                return this;
            }

            public Builder data(Object data) {
                payload.data = data;
                return this;
            }

            public Builder timestamp(long timestamp) {
                payload.timestamp = timestamp;
                return this;
            }

            public NotificationPayload build() {
                return payload;
            }
        }
    }
} 