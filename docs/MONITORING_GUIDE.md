# Guía de Monitoreo y Notificaciones

## 📊 **FASE 2: Monitoreo y Notificaciones**

Esta guía describe la implementación completa del sistema de monitoreo y notificaciones para Tournament Management.

## 🏗️ **Arquitectura del Sistema**

### **Stack de Monitoreo**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Prometheus    │    │   Grafana       │    │  AlertManager   │
│   (Métricas)    │    │   (Dashboard)   │    │   (Alertas)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Application   │
                    │   (Spring Boot) │
                    └─────────────────┘
```

### **Stack de Logs**
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Filebeat      │    │   Logstash      │    │  Elasticsearch  │
│   (Recolección) │───▶│   (Procesamiento)│───▶│   (Almacenamiento)│
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                         │
                    ┌─────────────────┐                 │
                    │   Kibana        │◀────────────────┘
                    │   (Visualización)│
                    └─────────────────┘
```

## 🚀 **Despliegue Rápido**

### **1. Desplegar Stack de Monitoreo**
```bash
# Hacer ejecutable el script
chmod +x scripts/deploy-monitoring.sh

# Ejecutar despliegue
./scripts/deploy-monitoring.sh
```

### **2. Verificar Servicios**
```bash
# Verificar que todos los servicios estén corriendo
docker-compose -f docker/docker-compose.monitoring.yml ps

# Ver logs en tiempo real
docker-compose -f docker/docker-compose.monitoring.yml logs -f
```

## 📈 **Servicios de Monitoreo**

### **Prometheus (Puerto 9090)**
- **Propósito**: Recolección y almacenamiento de métricas
- **URL**: http://localhost:9090
- **Configuración**: `monitoring/prometheus.yml`
- **Alertas**: `monitoring/rules/alerts.yml`

**Métricas Recolectadas**:
- Métricas de aplicación Spring Boot
- Métricas de JVM (memoria, CPU, GC)
- Métricas de base de datos PostgreSQL
- Métricas de Redis
- Métricas de RabbitMQ
- Métricas del sistema (Node Exporter)
- Métricas de contenedores (cAdvisor)

### **Grafana (Puerto 3000)**
- **Propósito**: Visualización de métricas y dashboards
- **URL**: http://localhost:3000
- **Usuario**: admin
- **Contraseña**: admin123
- **Dashboards**: `monitoring/grafana/dashboards/`

**Dashboards Disponibles**:
- Tournament Management Overview
- Application Performance
- Database Metrics
- System Resources
- Container Metrics

### **AlertManager (Puerto 9093)**
- **Propósito**: Gestión y envío de alertas
- **URL**: http://localhost:9093
- **Configuración**: `monitoring/alertmanager.yml`

**Canales de Notificación**:
- Email (SMTP)
- Slack
- PagerDuty
- Webhook personalizado

## 📝 **Sistema de Logs**

### **Elasticsearch (Puerto 9200)**
- **Propósito**: Almacenamiento de logs
- **URL**: http://localhost:9200
- **Índices**: `tournament-logs-*`

### **Kibana (Puerto 5601)**
- **Propósito**: Visualización y búsqueda de logs
- **URL**: http://localhost:5601

### **Logstash (Puerto 5044)**
- **Propósito**: Procesamiento de logs
- **Pipeline**: `monitoring/logstash/pipeline/logstash.conf`

### **Filebeat**
- **Propósito**: Recolección de logs de contenedores
- **Configuración**: `monitoring/filebeat/filebeat.yml`

## 🔔 **Sistema de Notificaciones**

### **Tipos de Notificaciones**

#### **1. Notificaciones de Torneo**
- Inicio de torneo
- Fin de torneo
- Actualizaciones de torneo
- Recordatorios de torneo

#### **2. Notificaciones de Tickets**
- Compra de ticket
- Cancelación de ticket
- Validación de ticket

#### **3. Notificaciones de Usuario**
- Registro de usuario
- Inicio de sesión
- Cambios de perfil

#### **4. Alertas del Sistema**
- Alta utilización de CPU/Memoria
- Errores de aplicación
- Problemas de base de datos
- Servicios caídos

### **Canales de Notificación**

#### **Email**
```java
// Configuración en application.yml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

#### **SMS (Twilio)**
```java
// Configuración en application.yml
twilio:
  account-sid: ${TWILIO_ACCOUNT_SID}
  auth-token: ${TWILIO_AUTH_TOKEN}
  phone-number: ${TWILIO_PHONE_NUMBER}
```

#### **Push Notifications (Firebase)**
```java
// Configuración en application.yml
firebase:
  project-id: ${FIREBASE_PROJECT_ID}
  private-key-id: ${FIREBASE_PRIVATE_KEY_ID}
  private-key: ${FIREBASE_PRIVATE_KEY}
  client-email: ${FIREBASE_CLIENT_EMAIL}
```

#### **WebSocket (Tiempo Real)**
```javascript
// Cliente WebSocket
const ws = new WebSocket('ws://localhost:8080/ws');
ws.onmessage = function(event) {
    const notification = JSON.parse(event.data);
    showNotification(notification);
};
```

## 📊 **Alertas Configuradas**

### **Alertas de Aplicación**
```yaml
# TournamentAppDown
expr: up{job="tournament-management"} == 0
for: 1m
severity: critical

# TournamentAppHighResponseTime
expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job="tournament-management"}[5m])) > 2
for: 5m
severity: warning

# TournamentAppHighErrorRate
expr: rate(http_server_requests_seconds_count{job="tournament-management", status=~"5.."}[5m]) / rate(http_server_requests_seconds_count{job="tournament-management"}[5m]) > 0.05
for: 5m
severity: critical
```

### **Alertas de Base de Datos**
```yaml
# PostgresDown
expr: up{job="postgres"} == 0
for: 1m
severity: critical

# PostgresHighConnections
expr: pg_stat_database_numbackends{job="postgres"} > 80
for: 5m
severity: warning
```

### **Alertas de Sistema**
```yaml
# HighCPUUsage
expr: 100 - (avg by(instance) (irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
for: 5m
severity: warning

# HighMemoryUsage
expr: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes > 0.85
for: 5m
severity: warning
```

## 🎥 **Sistema de Streaming**

### **Características**
- Transmisión en tiempo real de torneos
- Chat en vivo
- Estadísticas de espectadores
- Grabación de streams
- Calidad adaptable

### **Configuración**
```bash
# Instalar dependencias
npm install ws express cors

# Iniciar servicio de streaming
node streaming/streaming-service.js
```

### **API de Streaming**
```javascript
// Conectar a stream
ws.send(JSON.stringify({
    type: 'JOIN_STREAM',
    streamId: 'tournament-123',
    userId: 'user-456'
}));

// Enviar mensaje de chat
ws.send(JSON.stringify({
    type: 'CHAT_MESSAGE',
    streamId: 'tournament-123',
    userId: 'user-456',
    message: '¡Gran jugada!',
    username: 'player123'
}));
```

## 📱 **Dashboard Administrativo**

### **Características**
- Gestión de torneos en tiempo real
- Estadísticas de usuarios
- Monitoreo de tickets
- Analytics avanzados
- Configuración del sistema

### **Acceso**
- **URL**: http://localhost:8080/dashboard
- **Autenticación**: JWT Token
- **Roles**: Admin, Organizer, User

## 🔧 **Configuración Avanzada**

### **Variables de Entorno**
```bash
# Monitoreo
PROMETHEUS_RETENTION_TIME=200h
GRAFANA_ADMIN_PASSWORD=admin123
ELASTICSEARCH_HEAP_SIZE=512m

# Notificaciones
MAIL_USERNAME=notifications@tournament.com
MAIL_PASSWORD=your_password
TWILIO_ACCOUNT_SID=your_sid
TWILIO_AUTH_TOKEN=your_token
FIREBASE_PROJECT_ID=your_project_id

# Streaming
STREAMING_PORT=3001
STREAMING_MAX_BITRATE=5000
```

### **Personalización de Dashboards**
```json
{
  "dashboard": {
    "title": "Tournament Management Overview",
    "panels": [
      {
        "title": "Application Health",
        "type": "stat",
        "targets": [
          {
            "expr": "up{job=\"tournament-management\"}"
          }
        ]
      }
    ]
  }
}
```

## 🚨 **Troubleshooting**

### **Problemas Comunes**

#### **1. Prometheus no puede scrapear la aplicación**
```bash
# Verificar que la aplicación exponga métricas
curl http://localhost:8080/actuator/prometheus

# Verificar configuración de Prometheus
docker-compose -f docker/docker-compose.monitoring.yml logs prometheus
```

#### **2. Grafana no puede conectar a Prometheus**
```bash
# Verificar que Prometheus esté corriendo
curl http://localhost:9090/api/v1/targets

# Verificar configuración de datasource
curl http://localhost:3000/api/datasources
```

#### **3. AlertManager no envía notificaciones**
```bash
# Verificar configuración
docker-compose -f docker/docker-compose.monitoring.yml logs alertmanager

# Verificar conectividad SMTP
telnet smtp.gmail.com 587
```

#### **4. Logs no aparecen en Kibana**
```bash
# Verificar que Filebeat esté recolectando logs
docker-compose -f docker/docker-compose.monitoring.yml logs filebeat

# Verificar que Logstash esté procesando
docker-compose -f docker/docker-compose.monitoring.yml logs logstash

# Verificar índices en Elasticsearch
curl http://localhost:9200/_cat/indices
```

## 📈 **Métricas Clave**

### **KPIs de Aplicación**
- **Response Time**: < 2 segundos (95th percentile)
- **Error Rate**: < 1%
- **Uptime**: > 99.9%
- **Throughput**: > 1000 requests/segundo

### **KPIs de Base de Datos**
- **Connection Pool**: < 80% utilización
- **Query Time**: < 100ms promedio
- **Lock Wait Time**: < 50ms

### **KPIs de Sistema**
- **CPU Usage**: < 80%
- **Memory Usage**: < 85%
- **Disk Usage**: < 85%
- **Network I/O**: < 1GB/s

## 🔄 **Mantenimiento**

### **Backup de Datos**
```bash
# Backup de Prometheus
docker exec prometheus tar czf /prometheus/backup-$(date +%Y%m%d).tar.gz /prometheus

# Backup de Grafana
docker exec grafana tar czf /var/lib/grafana/backup-$(date +%Y%m%d).tar.gz /var/lib/grafana

# Backup de Elasticsearch
curl -X PUT "localhost:9200/_snapshot/backup_repo/snapshot_$(date +%Y%m%d)" -H 'Content-Type: application/json' -d '{}'
```

### **Rotación de Logs**
```yaml
# Configuración de rotación en logback-spring.xml
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <fileNamePattern>logs/tournament-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
    <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
        <maxFileSize>100MB</maxFileSize>
    </timeBasedFileNamingAndTriggeringPolicy>
    <maxHistory>30</maxHistory>
</rollingPolicy>
```

## 📚 **Recursos Adicionales**

### **Documentación Oficial**
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Elasticsearch Documentation](https://www.elastic.co/guide/)
- [AlertManager Documentation](https://prometheus.io/docs/alerting/latest/alertmanager/)

### **Comunidad**
- [Prometheus Community](https://prometheus.io/community/)
- [Grafana Community](https://community.grafana.com/)
- [Elastic Community](https://discuss.elastic.co/)

---

**¡El sistema de monitoreo y notificaciones está listo para producción!** 🎉 