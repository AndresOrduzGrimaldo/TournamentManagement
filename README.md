# Plataforma de Gestión de Torneos de Videojuegos

## Descripción
Sistema integral para la gestión de torneos de videojuegos que permite crear, administrar y monetizar eventos competitivos con integración de streaming en vivo.

## Características Principales
- ✅ Gestión de torneos gratuitos y pagados
- ✅ Venta de tickets con códigos QR únicos
- ✅ Integración con plataformas de streaming
- ✅ Sistema de notificaciones en tiempo real
- ✅ Dashboard de administración con métricas
- ✅ Seguridad y autenticación robusta

## Arquitectura
- **Backend**: Spring Boot (Java 17+)
- **Base de Datos**: PostgreSQL
- **Autenticación**: JWT/OAuth2
- **Monitoreo**: Prometheus + Grafana
- **CI/CD**: GitHub Actions + Docker

## Estructura del Proyecto
```
TournamentManagement/
├── src/
│   ├── main/java/com/tournament/
│   │   ├── domain/          # Entidades de dominio
│   │   ├── application/      # Casos de uso
│   │   ├── infrastructure/   # Implementaciones técnicas
│   │   └── presentation/     # Controladores REST
│   ├── test/                 # Pruebas unitarias e integración
│   └── resources/
│       ├── application.yml   # Configuración
│       └── db/migration/     # Scripts de base de datos
├── docs/                     # Documentación técnica
├── docker/                   # Configuración Docker
└── kubernetes/               # Manifiestos K8s
```

## Tecnologías
- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis (caché)
- RabbitMQ (mensajería)
- Docker & Kubernetes

## Estado del Desarrollo
🔄 En desarrollo - Fase inicial de configuración 