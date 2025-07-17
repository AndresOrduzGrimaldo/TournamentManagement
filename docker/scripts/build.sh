#!/bin/bash

# Script de construcción Docker para Tournament Management
# Uso: ./build.sh [dev|prod] [version]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
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

# Verificar argumentos
if [ $# -lt 1 ]; then
    error "Uso: $0 [dev|prod] [version]"
    echo "Ejemplos:"
    echo "  $0 dev"
    echo "  $0 prod 1.0.0"
    exit 1
fi

ENVIRONMENT=$1
VERSION=${2:-"latest"}

# Validar entorno
if [ "$ENVIRONMENT" != "dev" ] && [ "$ENVIRONMENT" != "prod" ]; then
    error "Entorno debe ser 'dev' o 'prod'"
fi

log "Iniciando construcción para entorno: $ENVIRONMENT"
log "Versión: $VERSION"

# Verificar que Docker esté ejecutándose
if ! docker info > /dev/null 2>&1; then
    error "Docker no está ejecutándose"
fi

# Verificar que el Dockerfile existe
if [ ! -f "../Dockerfile" ]; then
    error "Dockerfile no encontrado en el directorio padre"
fi

# Construir imagen
log "Construyendo imagen Docker..."
cd ..

if [ "$ENVIRONMENT" = "dev" ]; then
    docker build --target development -t tournament-management:dev .
    log "Imagen de desarrollo construida: tournament-management:dev"
else
    docker build --target production -t tournament-management:$VERSION .
    docker tag tournament-management:$VERSION tournament-management:latest
    log "Imagen de producción construida: tournament-management:$VERSION"
fi

# Verificar que la construcción fue exitosa
if [ $? -eq 0 ]; then
    log "Construcción completada exitosamente"
    
    # Mostrar información de la imagen
    log "Información de la imagen:"
    docker images tournament-management --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"
else
    error "Error durante la construcción"
fi

log "Proceso completado" 