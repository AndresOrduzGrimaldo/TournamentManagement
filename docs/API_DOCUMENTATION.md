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

### Usuarios de Prueba

Se han creado usuarios de prueba con la contraseña `admin123`:

- **Admin**: `admin` / `admin123`
- **Organizador**: `organizador1` / `admin123`
- **Participante 1**: `participante1` / `admin123`
- **Participante 2**: `participante2` / `admin123`

## Endpoints

### Autenticación

#### Registrar Usuario
```http
POST /auth/register
Content-Type: application/json

{
  "username": "nuevo_usuario",
  "email": "usuario@example.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "password": "password123",
  "role": "PARTICIPANT"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "nuevo_usuario",
    "email": "usuario@example.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "role": "PARTICIPANT",
    "fullName": "Juan Pérez"
  }
}
```

#### Iniciar Sesión
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### Validar Token
```http
POST /auth/validate?token=eyJhbGciOiJIUzUxMiJ9...
```

#### Refrescar Token
```http
POST /auth/refresh?token=eyJhbGciOiJIUzUxMiJ9...
```

#### Información del Usuario Actual
```http
GET /auth/me
Authorization: Bearer <token>
```

#### Cerrar Sesión
```http
POST /auth/logout
Authorization: Bearer <token>
```

### Torneos

#### Crear Torneo
```http
POST /tournaments
Authorization: Bearer <token>
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
    "firstName": "Administrador",
    "lastName": "Sistema",
    "role": "ADMIN",
    "fullName": "Administrador Sistema"
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
Authorization: Bearer <token>
```

#### Listar Torneos
```http
GET /tournaments
Authorization: Bearer <token>
```

#### Torneos por Organizador
```http
GET /tournaments/organizer/{organizerId}
Authorization: Bearer <token>
```

#### Torneos Abiertos
```http
GET /tournaments/open
Authorization: Bearer <token>
```

#### Actualizar Estado
```http
PUT /tournaments/{id}/status?status=REGISTRATION_OPEN
Authorization: Bearer <token>
```

### Tickets

#### Crear Ticket
```http
POST /tickets?userId=1&tournamentId=1
Authorization: Bearer <token>
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
Authorization: Bearer <token>
```

#### Obtener Ticket por QR
```http
GET /tickets/qr/{qrCode}
Authorization: Bearer <token>
```

#### Tickets por Usuario
```http
GET /tickets/user/{userId}
Authorization: Bearer <token>
```

#### Tickets por Torneo
```http
GET /tickets/tournament/{tournamentId}
Authorization: Bearer <token>
```

#### Validar Ticket
```http
POST /tickets/validate?qrCode=TICKET-ABC123DEF456GHI7
Authorization: Bearer <token>
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
Authorization: Bearer <token>
```

#### Generar Imagen QR
```http
GET /tickets/{id}/qr-image
Authorization: Bearer <token>
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
- `401 Unauthorized`: No autenticado o token inválido
- `403 Forbidden`: No autorizado
- `404 Not Found`: Recurso no encontrado
- `409 Conflict`: Conflicto (ej: torneo completo)
- `500 Internal Server Error`: Error interno del servidor

## Roles y Permisos

### ADMIN
- Acceso completo a todas las funcionalidades
- Puede crear torneos ilimitados
- Puede gestionar usuarios y torneos

### SUBADMIN
- Puede crear torneos (máximo 2 gratuitos)
- Puede gestionar sus propios torneos
- Puede asignar subadministradores

### PARTICIPANT
- Puede registrarse en torneos
- Puede comprar tickets
- Acceso limitado a funcionalidades

## Validaciones

### Usuarios
- Username: 3-50 caracteres, único
- Email: formato válido, único
- Password: 6-100 caracteres
- Nombre/Apellido: 2-50 caracteres

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

### 1. Registrar un Nuevo Usuario
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "nuevo_usuario",
    "email": "usuario@example.com",
    "firstName": "Juan",
    "lastName": "Pérez",
    "password": "password123",
    "role": "PARTICIPANT"
  }'
```

### 2. Iniciar Sesión
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### 3. Crear un Torneo (con autenticación)
```bash
# Primero obtener el token del login
TOKEN="eyJhbGciOiJIUzUxMiJ9..."

curl -X POST http://localhost:8080/api/tournaments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Torneo de FIFA 24",
    "description": "Torneo amistoso de FIFA",
    "categoryId": 5,
    "gameTypeId": 7,
    "organizerId": 1,
    "isFree": true,
    "maxParticipants": 32,
    "startDate": "2024-03-20T14:00:00",
    "endDate": "2024-03-20T22:00:00"
  }'
```

### 4. Comprar un Ticket
```bash
curl -X POST "http://localhost:8080/api/tickets?userId=2&tournamentId=1" \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Validar un Ticket
```bash
curl -X POST "http://localhost:8080/api/tickets/validate?qrCode=TICKET-ABC123DEF456GHI7" \
  -H "Authorization: Bearer $TOKEN"
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

## Seguridad

### JWT Token
- Algoritmo: HS512
- Expiración: 24 horas (configurable)
- Claims: userId, role, email, username

### Headers de Seguridad
La aplicación incluye headers de seguridad automáticamente:
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block

### CORS
Configurado para permitir peticiones desde cualquier origen en desarrollo:
- Origen: *
- Métodos: GET, POST, PUT, DELETE, OPTIONS
- Headers: *
- Credenciales: true 