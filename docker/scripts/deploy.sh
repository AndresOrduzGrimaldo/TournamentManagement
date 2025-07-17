#!/bin/bash

# Script de despliegue Docker para Tournament Management
# Uso: ./deploy.sh [dev|prod] [start|stop|restart|logs]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para mostrar mensajes
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

warning() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

# Verificar argumentos
if [ $# -lt 2 ]; then
    error "Uso: $0 [dev|prod] [start|stop|restart|logs|status|clean]"
    echo "Ejemplos:"
    echo "  $0 dev start"
    echo "  $0 prod stop"
    echo "  $0 dev logs"
    exit 1
fi

ENVIRONMENT=$1
ACTION=$2

# Validar entorno
if [ "$ENVIRONMENT" != "dev" ] && [ "$ENVIRONMENT" != "prod" ]; then
    error "Entorno debe ser 'dev' o 'prod'"
fi

# Validar acción
if [ "$ACTION" != "start" ] && [ "$ACTION" != "stop" ] && [ "$ACTION" != "restart" ] && [ "$ACTION" != "logs" ] && [ "$ACTION" != "status" ] && [ "$ACTION" != "clean" ]; then
    error "Acción debe ser 'start', 'stop', 'restart', 'logs', 'status' o 'clean'"
fi

# Definir archivo de compose
COMPOSE_FILE="docker-compose.${ENVIRONMENT}.yml"

# Verificar que el archivo existe
if [ ! -f "$COMPOSE_FILE" ]; then
    error "Archivo $COMPOSE_FILE no encontrado"
fi

log "Entorno: $ENVIRONMENT"
log "Acción: $ACTION"

# Función para cargar variables de entorno
load_env() {
    if [ -f ".env.${ENVIRONMENT}" ]; then
        log "Cargando variables de entorno desde .env.${ENVIRONMENT}"
        export $(cat .env.${ENVIRONMENT} | xargs)
    else
        warning "Archivo .env.${ENVIRONMENT} no encontrado, usando valores por defecto"
    fi
}

# Función para verificar salud de los servicios
check_health() {
    log "Verificando salud de los servicios..."
    sleep 10
    
    # Verificar PostgreSQL
    if docker-compose -f $COMPOSE_FILE ps postgres | grep -q "Up"; then
        log "✅ PostgreSQL está ejecutándose"
    else
        error "❌ PostgreSQL no está ejecutándose"
    fi
    
    # Verificar Redis
    if docker-compose -f $COMPOSE_FILE ps redis | grep -q "Up"; then
        log "✅ Redis está ejecutándose"
    else
        error "❌ Redis no está ejecutándose"
    fi
    
    # Verificar RabbitMQ
    if docker-compose -f $COMPOSE_FILE ps rabbitmq | grep -q "Up"; then
        log "✅ RabbitMQ está ejecutándose"
    else
        error "❌ RabbitMQ no está ejecutándose"
    fi
    
    # Verificar aplicación
    if docker-compose -f $COMPOSE_FILE ps tournament-app | grep -q "Up"; then
        log "✅ Aplicación está ejecutándose"
    else
        error "❌ Aplicación no está ejecutándose"
    fi
}

# Ejecutar acción
case $ACTION in
    "start")
        log "Iniciando servicios..."
        load_env
        docker-compose -f $COMPOSE_FILE up -d
        check_health
        log "Servicios iniciados exitosamente"
        info "Aplicación disponible en: http://localhost:8080"
        info "RabbitMQ Management: http://localhost:15672"
        ;;
    
    "stop")
        log "Deteniendo servicios..."
        docker-compose -f $COMPOSE_FILE down
        log "Servicios detenidos"
        ;;
    
    "restart")
        log "Reiniciando servicios..."
        docker-compose -f $COMPOSE_FILE down
        load_env
        docker-compose -f $COMPOSE_FILE up -d
        check_health
        log "Servicios reiniciados exitosamente"
        ;;
    
    "logs")
        log "Mostrando logs..."
        docker-compose -f $COMPOSE_FILE logs -f
        ;;
    
    "status")
        log "Estado de los servicios:"
        docker-compose -f $COMPOSE_FILE ps
        ;;
    
    "clean")
        warning "Esta acción eliminará todos los contenedores, volúmenes y redes"
        read -p "¿Estás seguro? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            log "Limpiando recursos Docker..."
            docker-compose -f $COMPOSE_FILE down -v --remove-orphans
            docker system prune -f
            log "Limpieza completada"
        else
            log "Operación cancelada"
        fi
        ;;
esac

log "Proceso completado" 