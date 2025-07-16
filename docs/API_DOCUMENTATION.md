# Documentación de la API - Plataforma de Gestión de Torneos

## Descripción General

La API de gestión de torneos proporciona endpoints para crear, administrar y participar en torneos de videojuegos. La API está construida con Spring Boot 3.x y utiliza JWT para autenticación.

## Base URL

```
http://localhost:8080/api
```

## Autenticación

La API utiliza JWT (JSON Web Tokens) para autenticación. Incluye el token en el header de las peticiones:

```
Authorization: Bearer <token>
```

## Endpoints

### Torneos

#### Crear Torneo
```http
POST /tournaments
Content-Type: application/json

{
  "name": "League of Legends Championship",
  "description": "Torneo nacional de LoL",
  "categoryId": 1,
  "gameTypeId": 1,
  "organizerId": 1,
  "isFree": false,
  "price": 25.00,
  "maxParticipants": 100,
  "startDate": "2024-03-15T10:00:00",
  "endDate": "2024-03-15T18:00:00",
  "commissionPercentage": 5.0
}
```

**Respuesta:**
```json
{
  "id": 1,
  "name": "League of Legends Championship",
  "description": "Torneo nacional de LoL",
  "category": {
    "id": 1,
    "code": "MOBA",
    "description": "Multiplayer Online Battle Arena",
    "alias": "MOBA"
  },
  "gameType": {
    "id": 1,
    "code": "LOL",
    "fullName": "League of Legends",
    "playersCount": 5,
    "category": {...}
  },
  "organizer": {
    "id": 1,
    "username": "admin",
    "email": "admin@tournament.com",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN",
    "fullName": "Admin User"
  },
  "isFree": false,
  "price": 25.00,
  "maxParticipants": 100,
  "currentParticipants": 0,
  "startDate": "2024-03-15T10:00:00",
  "endDate": "2024-03-15T18:00:00",
  "status": "DRAFT",
  "commissionPercentage": 5.0,
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:00:00"
}
```

#### Obtener Torneo
```http
GET /tournaments/{id}
```

#### Listar Torneos
```http
GET /tournaments
```

#### Torneos por Organizador
```http
GET /tournaments/organizer/{organizerId}
```

#### Torneos Abiertos
```http
GET /tournaments/open
```

#### Actualizar Estado
```http
PUT /tournaments/{id}/status?status=REGISTRATION_OPEN
```

### Tickets

#### Crear Ticket
```http
POST /tickets?userId=1&tournamentId=1
```

**Respuesta:**
```json
{
  "id": 1,
  "user": {...},
  "tournament": {...},
  "qrCode": "TICKET-ABC123DEF456GHI7",
  "uniqueCode": "TM-XYZ789ABC123",
  "purchaseDate": "2024-01-15T10:30:00",
  "price": 25.00,
  "serviceFee": 1.25,
  "totalAmount": 26.25,
  "status": "ACTIVE",
  "usedAt": null,
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Obtener Ticket
```http
GET /tickets/{id}
```

#### Obtener Ticket por QR
```http
GET /tickets/qr/{qrCode}
```

#### Tickets por Usuario
```http
GET /tickets/user/{userId}
```

#### Tickets por Torneo
```http
GET /tickets/tournament/{tournamentId}
```

#### Validar Ticket
```http
POST /tickets/validate?qrCode=TICKET-ABC123DEF456GHI7
```

**Respuesta:**
```json
{
  "valid": true,
  "message": "Ticket validado exitosamente"
}
```

#### Cancelar Ticket
```http
POST /tickets/{id}/cancel
```

#### Generar Imagen QR
```http
GET /tickets/{id}/qr-image
```

**Respuesta:**
```json
{
  "qrCode": "TICKET-ABC123DEF456GHI7",
  "qrImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

## Códigos de Estado HTTP

- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado exitosamente
- `400 Bad Request`: Datos de entrada inválidos
- `401 Unauthorized`: No autenticado
- `403 Forbidden`: No autorizado
- `404 Not Found`: Recurso no encontrado
- `409 Conflict`: Conflicto (ej: torneo completo)
- `500 Internal Server Error`: Error interno del servidor

## Validaciones

### Torneos
- Nombre: 3-200 caracteres
- Precio: 0.00 - 9999.99
- Participantes: 2-1000
- Fechas: deben ser futuras
- Comisión: 0-100%

### Tickets
- Usuario y torneo deben existir
- Torneo debe estar abierto para registro
- Torneo no debe estar completo
- Códigos QR y únicos son generados automáticamente

## Ejemplos de Uso

### Crear un Torneo Gratuito
```bash
curl -X POST http://localhost:8080/api/tournaments \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Torneo Gratuito de FIFA",
    "description": "Torneo amistoso de FIFA 24",
    "categoryId": 5,
    "gameTypeId": 7,
    "organizerId": 1,
    "isFree": true,
    "maxParticipants": 32,
    "startDate": "2024-03-20T14:00:00",
    "endDate": "2024-03-20T22:00:00"
  }'
```

### Comprar un Ticket
```bash
curl -X POST "http://localhost:8080/api/tickets?userId=2&tournamentId=1"
```

### Validar un Ticket
```bash
curl -X POST "http://localhost:8080/api/tickets/validate?qrCode=TICKET-ABC123DEF456GHI7"
```

## Swagger UI

La documentación interactiva está disponible en:
```
http://localhost:8080/api/swagger-ui.html
```

## Monitoreo

### Health Check
```http
GET /actuator/health
```

### Métricas
```http
GET /actuator/metrics
```

### Prometheus
```http
GET /actuator/prometheus
``` 