const WebSocket = require('ws');
const http = require('http');
const express = require('express');
const cors = require('cors');
const path = require('path');

/**
 * Servicio de Streaming para Tournament Management
 * Permite transmitir torneos en tiempo real usando WebRTC y WebSocket
 */
class StreamingService {
    constructor() {
        this.app = express();
        this.server = http.createServer(this.app);
        this.wss = new WebSocket.Server({ server: this.server });
        
        this.streams = new Map(); // Almacena streams activos
        this.viewers = new Map(); // Almacena espectadores por stream
        this.rooms = new Map(); // Almacena salas de chat
        
        this.setupMiddleware();
        this.setupWebSocket();
        this.setupRoutes();
    }

    /**
     * Configura middleware de Express
     */
    setupMiddleware() {
        this.app.use(cors());
        this.app.use(express.json());
        this.app.use(express.static(path.join(__dirname, 'public')));
    }

    /**
     * Configura WebSocket para comunicación en tiempo real
     */
    setupWebSocket() {
        this.wss.on('connection', (ws, req) => {
            console.log('Nueva conexión WebSocket establecida');

            ws.on('message', (message) => {
                try {
                    const data = JSON.parse(message);
                    this.handleWebSocketMessage(ws, data);
                } catch (error) {
                    console.error('Error procesando mensaje WebSocket:', error);
                }
            });

            ws.on('close', () => {
                this.handleClientDisconnect(ws);
            });

            ws.on('error', (error) => {
                console.error('Error en WebSocket:', error);
            });
        });
    }

    /**
     * Maneja mensajes WebSocket entrantes
     * @param {WebSocket} ws - Conexión WebSocket
     * @param {Object} data - Datos del mensaje
     */
    handleWebSocketMessage(ws, data) {
        switch (data.type) {
            case 'JOIN_STREAM':
                this.joinStream(ws, data.streamId, data.userId);
                break;
            case 'LEAVE_STREAM':
                this.leaveStream(ws, data.streamId, data.userId);
                break;
            case 'CHAT_MESSAGE':
                this.handleChatMessage(ws, data);
                break;
            case 'STREAM_START':
                this.startStream(ws, data);
                break;
            case 'STREAM_END':
                this.endStream(ws, data);
                break;
            case 'VIEWER_COUNT':
                this.getViewerCount(ws, data.streamId);
                break;
            case 'STREAM_STATS':
                this.getStreamStats(ws, data.streamId);
                break;
            default:
                console.log('Tipo de mensaje desconocido:', data.type);
        }
    }

    /**
     * Une un usuario a un stream
     * @param {WebSocket} ws - Conexión WebSocket
     * @param {string} streamId - ID del stream
     * @param {string} userId - ID del usuario
     */
    joinStream(ws, streamId, userId) {
        console.log(`Usuario ${userId} se une al stream ${streamId}`);

        // Agregar usuario a la lista de espectadores
        if (!this.viewers.has(streamId)) {
            this.viewers.set(streamId, new Set());
        }
        this.viewers.get(streamId).add(userId);

        // Agregar metadata de conexión
        ws.streamId = streamId;
        ws.userId = userId;

        // Notificar a otros espectadores
        this.broadcastToStream(streamId, {
            type: 'VIEWER_JOINED',
            userId: userId,
            viewerCount: this.viewers.get(streamId).size
        });

        // Enviar confirmación al usuario
        ws.send(JSON.stringify({
            type: 'STREAM_JOINED',
            streamId: streamId,
            viewerCount: this.viewers.get(streamId).size
        }));
    }

    /**
     * Remueve un usuario de un stream
     * @param {WebSocket} ws - Conexión WebSocket
     * @param {string} streamId - ID del stream
     * @param {string} userId - ID del usuario
     */
    leaveStream(ws, streamId, userId) {
        console.log(`Usuario ${userId} sale del stream ${streamId}`);

        // Remover usuario de la lista de espectadores
        if (this.viewers.has(streamId)) {
            this.viewers.get(streamId).delete(userId);
        }

        // Notificar a otros espectadores
        this.broadcastToStream(streamId, {
            type: 'VIEWER_LEFT',
            userId: userId,
            viewerCount: this.viewers.get(streamId)?.size || 0
        });
    }

    /**
     * Maneja mensajes de chat
     * @param {WebSocket} ws - Conexión WebSocket
     * @param {Object} data - Datos del mensaje
     */
    handleChatMessage(ws, data) {
        const { streamId, userId, message, username } = data;
        
        console.log(`Chat en stream ${streamId}: ${username}: ${message}`);

        // Broadcast del mensaje a todos los espectadores del stream
        this.broadcastToStream(streamId, {
            type: 'CHAT_MESSAGE',
            userId: userId,
            username: username,
            message: message,
            timestamp: Date.now()
        });
    }

    /**
     * Inicia un nuevo stream
     * @param {WebSocket} ws - Conexión WebSocket
     * @param {Object} data - Datos del stream
     */
    startStream(ws, data) {
        const { streamId, tournamentId, streamerId, title, description } = data;
        
        console.log(`Iniciando stream ${streamId} para torneo ${tournamentId}`);

        // Crear nuevo stream
        this.streams.set(streamId, {
            id: streamId,
            tournamentId: tournamentId,
            streamerId: streamerId,
            title: title,
            description: description,
            startTime: Date.now(),
            isLive: true,
            viewerCount: 0,
            peakViewers: 0
        });

        // Agregar metadata de conexión
        ws.streamId = streamId;
        ws.isStreamer = true;

        // Notificar inicio del stream
        this.broadcastToAll({
            type: 'STREAM_STARTED',
            streamId: streamId,
            tournamentId: tournamentId,
            title: title,
            streamerId: streamerId
        });
    }

    /**
     * Termina un stream
     * @param {WebSocket} ws - Conexión WebSocket
     * @param {Object} data - Datos del stream
     */
    endStream(ws, data) {
        const { streamId } = data;
        
        console.log(`Terminando stream ${streamId}`);

        // Marcar stream como terminado
        if (this.streams.has(streamId)) {
            this.streams.get(streamId).isLive = false;
            this.streams.get(streamId).endTime = Date.now();
        }

        // Notificar fin del stream
        this.broadcastToStream(streamId, {
            type: 'STREAM_ENDED',
            streamId: streamId
        });

        // Limpiar espectadores
        this.viewers.delete(streamId);
    }

    /**
     * Obtiene el número de espectadores de un stream
     * @param {WebSocket} ws - Conexión WebSocket
     * @param {string} streamId - ID del stream
     */
    getViewerCount(ws, streamId) {
        const count = this.viewers.get(streamId)?.size || 0;
        
        ws.send(JSON.stringify({
            type: 'VIEWER_COUNT',
            streamId: streamId,
            count: count
        }));
    }

    /**
     * Obtiene estadísticas de un stream
     * @param {WebSocket} ws - Conexión WebSocket
     * @param {string} streamId - ID del stream
     */
    getStreamStats(ws, streamId) {
        const stream = this.streams.get(streamId);
        const viewerCount = this.viewers.get(streamId)?.size || 0;
        
        if (stream) {
            ws.send(JSON.stringify({
                type: 'STREAM_STATS',
                streamId: streamId,
                stats: {
                    viewerCount: viewerCount,
                    startTime: stream.startTime,
                    duration: Date.now() - stream.startTime,
                    isLive: stream.isLive
                }
            }));
        }
    }

    /**
     * Maneja la desconexión de un cliente
     * @param {WebSocket} ws - Conexión WebSocket
     */
    handleClientDisconnect(ws) {
        console.log('Cliente desconectado');

        if (ws.streamId && ws.userId) {
            this.leaveStream(ws, ws.streamId, ws.userId);
        }

        if (ws.streamId && ws.isStreamer) {
            this.endStream(ws, { streamId: ws.streamId });
        }
    }

    /**
     * Transmite mensaje a todos los espectadores de un stream
     * @param {string} streamId - ID del stream
     * @param {Object} message - Mensaje a transmitir
     */
    broadcastToStream(streamId, message) {
        this.wss.clients.forEach((client) => {
            if (client.readyState === WebSocket.OPEN && client.streamId === streamId) {
                client.send(JSON.stringify(message));
            }
        });
    }

    /**
     * Transmite mensaje a todos los clientes conectados
     * @param {Object} message - Mensaje a transmitir
     */
    broadcastToAll(message) {
        this.wss.clients.forEach((client) => {
            if (client.readyState === WebSocket.OPEN) {
                client.send(JSON.stringify(message));
            }
        });
    }

    /**
     * Configura rutas de la API REST
     */
    setupRoutes() {
        // Obtener streams activos
        this.app.get('/api/streams', (req, res) => {
            const activeStreams = Array.from(this.streams.values())
                .filter(stream => stream.isLive)
                .map(stream => ({
                    ...stream,
                    viewerCount: this.viewers.get(stream.id)?.size || 0
                }));
            
            res.json(activeStreams);
        });

        // Obtener información de un stream específico
        this.app.get('/api/streams/:streamId', (req, res) => {
            const stream = this.streams.get(req.params.streamId);
            if (stream) {
                res.json({
                    ...stream,
                    viewerCount: this.viewers.get(stream.id)?.size || 0
                });
            } else {
                res.status(404).json({ error: 'Stream no encontrado' });
            }
        });

        // Obtener estadísticas de streaming
        this.app.get('/api/streams/:streamId/stats', (req, res) => {
            const stream = this.streams.get(req.params.streamId);
            if (stream) {
                res.json({
                    streamId: stream.id,
                    viewerCount: this.viewers.get(stream.id)?.size || 0,
                    startTime: stream.startTime,
                    duration: Date.now() - stream.startTime,
                    isLive: stream.isLive
                });
            } else {
                res.status(404).json({ error: 'Stream no encontrado' });
            }
        });

        // Health check
        this.app.get('/health', (req, res) => {
            res.json({
                status: 'OK',
                activeStreams: this.streams.size,
                totalViewers: Array.from(this.viewers.values())
                    .reduce((total, viewers) => total + viewers.size, 0),
                connections: this.wss.clients.size
            });
        });
    }

    /**
     * Inicia el servidor de streaming
     * @param {number} port - Puerto del servidor
     */
    start(port = 3001) {
        this.server.listen(port, () => {
            console.log(`Servicio de streaming iniciado en puerto ${port}`);
            console.log(`WebSocket disponible en ws://localhost:${port}`);
            console.log(`API REST disponible en http://localhost:${port}/api`);
        });
    }

    /**
     * Detiene el servidor de streaming
     */
    stop() {
        this.server.close(() => {
            console.log('Servicio de streaming detenido');
        });
    }
}

// Exportar la clase para uso en otros módulos
module.exports = StreamingService;

// Si se ejecuta directamente, iniciar el servidor
if (require.main === module) {
    const streamingService = new StreamingService();
    streamingService.start(process.env.PORT || 3001);
} 