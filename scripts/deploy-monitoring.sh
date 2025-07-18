#!/bin/bash

# Script de despliegue para el stack de monitoreo
# Tournament Management - FASE 2: Monitoreo y Notificaciones

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para logging
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Verificar que Docker esté instalado
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker no está instalado. Por favor instala Docker primero."
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        error "Docker no está ejecutándose. Por favor inicia Docker."
        exit 1
    fi
    
    success "Docker está disponible"
}

# Verificar que Docker Compose esté instalado
check_docker_compose() {
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose no está instalado. Por favor instala Docker Compose primero."
        exit 1
    fi
    
    success "Docker Compose está disponible"
}

# Crear directorios necesarios
create_directories() {
    log "Creando directorios para monitoreo..."
    
    mkdir -p monitoring/rules
    mkdir -p monitoring/grafana/dashboards
    mkdir -p monitoring/grafana/datasources
    mkdir -p monitoring/logstash/pipeline
    mkdir -p monitoring/logstash/config
    mkdir -p monitoring/filebeat
    mkdir -p dashboard
    mkdir -p streaming/public
    
    success "Directorios creados"
}

# Configurar Grafana
setup_grafana() {
    log "Configurando Grafana..."
    
    # Crear datasource de Prometheus
    cat > monitoring/grafana/datasources/prometheus.yml << EOF
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
EOF

    # Crear dashboard de Tournament Management
    if [ ! -f monitoring/grafana/dashboards/tournament-overview.json ]; then
        log "Dashboard de Tournament Management ya existe"
    fi
    
    success "Grafana configurado"
}

# Configurar Logstash
setup_logstash() {
    log "Configurando Logstash..."
    
    # Configuración de Logstash
    cat > monitoring/logstash/config/logstash.yml << EOF
http.host: "0.0.0.0"
xpack.monitoring.elasticsearch.hosts: [ "http://elasticsearch:9200" ]
EOF

    # Pipeline de Logstash
    cat > monitoring/logstash/pipeline/logstash.conf << EOF
input {
  beats {
    port => 5044
  }
}

filter {
  if [fields][service] == "tournament-management" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} %{GREEDYDATA:message}" }
    }
    date {
      match => [ "timestamp", "yyyy-MM-dd HH:mm:ss" ]
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "tournament-logs-%{+YYYY.MM.dd}"
  }
}
EOF

    success "Logstash configurado"
}

# Configurar Filebeat
setup_filebeat() {
    log "Configurando Filebeat..."
    
    cat > monitoring/filebeat/filebeat.yml << EOF
filebeat.inputs:
- type: container
  paths:
    - '/var/lib/docker/containers/*/*.log'

processors:
  - add_docker_metadata:
      host: "unix:///var/run/docker.sock"

output.logstash:
  hosts: ["logstash:5044"]

logging.json: true
EOF

    success "Filebeat configurado"
}

# Desplegar stack de monitoreo
deploy_monitoring() {
    log "Desplegando stack de monitoreo..."
    
    # Detener stack existente si está corriendo
    if docker-compose -f docker/docker-compose.monitoring.yml ps | grep -q "Up"; then
        warning "Deteniendo stack de monitoreo existente..."
        docker-compose -f docker/docker-compose.monitoring.yml down
    fi
    
    # Iniciar stack de monitoreo
    docker-compose -f docker/docker-compose.monitoring.yml up -d
    
    success "Stack de monitoreo desplegado"
}

# Verificar servicios
check_services() {
    log "Verificando servicios de monitoreo..."
    
    local services=("prometheus" "grafana" "alertmanager" "elasticsearch" "kibana")
    local ports=(9090 3000 9093 9200 5601)
    
    for i in "${!services[@]}"; do
        local service=${services[$i]}
        local port=${ports[$i]}
        
        log "Verificando $service en puerto $port..."
        
        # Esperar hasta que el servicio esté disponible
        local max_attempts=30
        local attempt=1
        
        while [ $attempt -le $max_attempts ]; do
            if curl -s "http://localhost:$port" > /dev/null 2>&1; then
                success "$service está disponible en puerto $port"
                break
            fi
            
            if [ $attempt -eq $max_attempts ]; then
                error "$service no está disponible después de $max_attempts intentos"
                return 1
            fi
            
            log "Esperando que $service esté disponible... (intento $attempt/$max_attempts)"
            sleep 2
            ((attempt++))
        done
    done
    
    success "Todos los servicios están disponibles"
}

# Configurar alertas
setup_alerts() {
    log "Configurando alertas..."
    
    # Verificar que AlertManager esté disponible
    if curl -s "http://localhost:9093" > /dev/null 2>&1; then
        success "AlertManager está disponible"
    else
        warning "AlertManager no está disponible, las alertas no se configurarán"
        return
    fi
    
    # TODO: Configurar alertas específicas via API de AlertManager
    log "Alertas configuradas (configuración manual requerida)"
}

# Mostrar información de acceso
show_access_info() {
    echo ""
    echo "=========================================="
    echo "    STACK DE MONITOREO DESPLEGADO"
    echo "=========================================="
    echo ""
    echo "Servicios disponibles:"
    echo "  • Grafana (Dashboard): http://localhost:3000"
    echo "    - Usuario: admin"
    echo "    - Contraseña: admin123"
    echo ""
    echo "  • Prometheus (Métricas): http://localhost:9090"
    echo ""
    echo "  • AlertManager (Alertas): http://localhost:9093"
    echo ""
    echo "  • Kibana (Logs): http://localhost:5601"
    echo ""
    echo "  • Elasticsearch (API): http://localhost:9200"
    echo ""
    echo "  • Jaeger (Trazabilidad): http://localhost:16686"
    echo ""
    echo "  • cAdvisor (Contenedores): http://localhost:8080"
    echo ""
    echo "Comandos útiles:"
    echo "  • Ver logs: docker-compose -f docker/docker-compose.monitoring.yml logs -f"
    echo "  • Detener: docker-compose -f docker/docker-compose.monitoring.yml down"
    echo "  • Reiniciar: docker-compose -f docker/docker-compose.monitoring.yml restart"
    echo ""
}

# Función principal
main() {
    log "Iniciando despliegue del stack de monitoreo..."
    
    check_docker
    check_docker_compose
    create_directories
    setup_grafana
    setup_logstash
    setup_filebeat
    deploy_monitoring
    
    log "Esperando que los servicios se inicien..."
    sleep 30
    
    check_services
    setup_alerts
    show_access_info
    
    success "Despliegue del stack de monitoreo completado exitosamente!"
}

# Ejecutar función principal
main "$@" 