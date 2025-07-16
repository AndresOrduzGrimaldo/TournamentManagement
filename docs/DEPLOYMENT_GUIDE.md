# Guía de Despliegue - Plataforma de Gestión de Torneos

## Requisitos Previos

- Java 17 o superior
- Maven 3.6+
- Docker y Docker Compose
- PostgreSQL 15+
- Redis 7+
- RabbitMQ 3.9+

## Configuración Local

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd TournamentManagement
```

### 2. Configurar Variables de Entorno

Crear archivo `.env` en la raíz del proyecto:

```env
# Base de Datos
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tournament_db
DB_USERNAME=postgres
DB_PASSWORD=password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# JWT
JWT_SECRET=your-secret-key-here-must-be-at-least-256-bits-long
JWT_EXPIRATION=86400000

# Configuración de Torneos
COMMISSION_PERCENTAGE=5.0
MAX_FREE_TOURNAMENTS=2
MAX_FREE_PARTICIPANTS=50

# Streaming (opcional)
TWITCH_CLIENT_ID=
TWITCH_CLIENT_SECRET=
DISCORD_WEBHOOK_URL=
ZOOM_API_KEY=
ZOOM_API_SECRET=
```

### 3. Ejecutar con Docker Compose

```bash
# Construir y ejecutar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f tournament-app

# Detener servicios
docker-compose down
```

### 4. Ejecutar Localmente

#### Opción A: Con Maven

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar tests
mvn test

# Ejecutar la aplicación
mvn spring-boot:run
```

#### Opción B: Con JAR

```bash
# Compilar y empaquetar
mvn clean package

# Ejecutar JAR
java -jar target/tournament-management-1.0.0.jar
```

## Configuración de Base de Datos

### 1. Crear Base de Datos

```sql
CREATE DATABASE tournament_db;
CREATE USER tournament_user WITH PASSWORD 'tournament_pass';
GRANT ALL PRIVILEGES ON DATABASE tournament_db TO tournament_user;
```

### 2. Ejecutar Migraciones

Las migraciones se ejecutan automáticamente al iniciar la aplicación con Flyway.

## Configuración de Monitoreo

### 1. Prometheus

Crear archivo `monitoring/prometheus.yml`:

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'tournament-app'
    static_configs:
      - targets: ['tournament-app:8080']
    metrics_path: '/actuator/prometheus'
```

### 2. Grafana

Acceder a Grafana en `http://localhost:3000`:
- Usuario: `admin`
- Contraseña: `admin`

Configurar datasource de Prometheus:
- URL: `http://prometheus:9090`

## Despliegue en Producción

### 1. AWS (EC2 + RDS)

#### Preparar Instancia EC2

```bash
# Actualizar sistema
sudo yum update -y

# Instalar Java 17
sudo yum install java-17-amazon-corretto -y

# Instalar Docker
sudo yum install docker -y
sudo service docker start
sudo usermod -a -G docker ec2-user

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### Configurar RDS

1. Crear instancia RDS PostgreSQL
2. Configurar Security Group
3. Actualizar variables de entorno con endpoint de RDS

#### Desplegar Aplicación

```bash
# Clonar repositorio
git clone <repository-url>
cd TournamentManagement

# Configurar variables de producción
cp .env.example .env
# Editar .env con valores de producción

# Ejecutar con Docker Compose
docker-compose -f docker-compose.prod.yml up -d
```

### 2. Azure (App Services + CosmosDB)

#### Configurar App Service

1. Crear App Service con Java 17
2. Configurar variables de entorno en Azure Portal
3. Conectar con CosmosDB

#### Desplegar desde Azure DevOps

```yaml
# azure-pipelines.yml
trigger:
- main

pool:
  vmImage: 'ubuntu-latest'

steps:
- task: Maven@3
  inputs:
    mavenPomFile: 'pom.xml'
    goals: 'package'
    publishJUnitResults: true

- task: ArchiveFiles@2
  inputs:
    rootFolderOrFile: 'target'
    includeRootFolder: false
    archiveType: 'zip'
    archiveFile: '$(Build.ArtifactStagingDirectory)/$(Build.BuildId).zip'

- task: AzureWebApp@1
  inputs:
    azureSubscription: 'Your-Azure-Subscription'
    appName: 'tournament-management'
    package: '$(Build.ArtifactStagingDirectory)/$(Build.BuildId).zip'
```

### 3. Kubernetes

#### Crear Namespace

```bash
kubectl create namespace tournament
```

#### Aplicar ConfigMap

```yaml
# k8s/configmap.yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: tournament-config
  namespace: tournament
data:
  SPRING_PROFILES_ACTIVE: "kubernetes"
  DB_HOST: "postgres-service"
  REDIS_HOST: "redis-service"
  RABBITMQ_HOST: "rabbitmq-service"
```

#### Aplicar Secrets

```yaml
# k8s/secrets.yml
apiVersion: v1
kind: Secret
metadata:
  name: tournament-secrets
  namespace: tournament
type: Opaque
data:
  DB_PASSWORD: <base64-encoded-password>
  JWT_SECRET: <base64-encoded-secret>
```

#### Desplegar Aplicación

```bash
# Aplicar configuración
kubectl apply -f k8s/configmap.yml
kubectl apply -f k8s/secrets.yml

# Desplegar aplicación
kubectl apply -f k8s/deployment.yml
kubectl apply -f k8s/service.yml
kubectl apply -f k8s/ingress.yml
```

## Monitoreo en Producción

### 1. Health Checks

```bash
# Verificar estado de la aplicación
curl http://localhost:8080/api/actuator/health

# Verificar métricas
curl http://localhost:8080/api/actuator/metrics
```

### 2. Logs

```bash
# Ver logs de la aplicación
docker-compose logs -f tournament-app

# En Kubernetes
kubectl logs -f deployment/tournament-app -n tournament
```

### 3. Alertas

Configurar alertas en Grafana para:
- CPU > 80%
- Memoria > 85%
- Error rate > 5%
- Response time > 2s

## Troubleshooting

### Problemas Comunes

#### 1. Error de Conexión a Base de Datos

```bash
# Verificar que PostgreSQL esté ejecutándose
docker-compose ps postgres

# Verificar logs
docker-compose logs postgres

# Conectar manualmente
docker-compose exec postgres psql -U postgres -d tournament_db
```

#### 2. Error de Redis

```bash
# Verificar estado de Redis
docker-compose exec redis redis-cli ping

# Verificar logs
docker-compose logs redis
```

#### 3. Error de RabbitMQ

```bash
# Verificar estado de RabbitMQ
curl -u guest:guest http://localhost:15672/api/overview

# Verificar logs
docker-compose logs rabbitmq
```

#### 4. Problemas de Memoria

```bash
# Aumentar heap size
export JAVA_OPTS="-Xms1g -Xmx2g"
java $JAVA_OPTS -jar target/tournament-management-1.0.0.jar
```

### Logs de Debug

Habilitar logs de debug en `application.yml`:

```yaml
logging:
  level:
    com.tournament: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

## Seguridad

### 1. Configurar HTTPS

```yaml
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: your-password
    key-store-type: PKCS12
```

### 2. Configurar CORS

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://your-domain.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### 3. Rate Limiting

```java
@Configuration
public class RateLimitConfig {
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0); // 100 requests per second
    }
}
```

## Backup y Recuperación

### 1. Backup de Base de Datos

```bash
# Backup manual
docker-compose exec postgres pg_dump -U postgres tournament_db > backup.sql

# Backup automático (cron)
0 2 * * * docker-compose exec postgres pg_dump -U postgres tournament_db > /backups/tournament_$(date +\%Y\%m\%d).sql
```

### 2. Restaurar Base de Datos

```bash
# Restaurar desde backup
docker-compose exec -T postgres psql -U postgres tournament_db < backup.sql
```

## Escalabilidad

### 1. Escalar Horizontalmente

```bash
# Escalar aplicación
docker-compose up -d --scale tournament-app=3

# En Kubernetes
kubectl scale deployment tournament-app --replicas=5
```

### 2. Configurar Load Balancer

```yaml
# k8s/ingress.yml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: tournament-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: tournament.your-domain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: tournament-service
            port:
              number: 8080
``` 