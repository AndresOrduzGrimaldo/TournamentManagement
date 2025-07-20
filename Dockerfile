# Dockerfile multi-stage para la aplicación de gestión de torneos
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Etiquetas de metadatos
LABEL maintainer="Tournament Management Team"
LABEL version="1.0.0"
LABEL description="Plataforma de Gestión de Torneos de Videojuegos"

# Crear directorio de trabajo
WORKDIR /build

# Copiar archivo de configuración Maven
COPY pom.xml .

# Descargar dependencias (capa de caché)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir aplicación
RUN mvn clean package -DskipTests

# ===== TARGET: DEVELOPMENT =====
FROM eclipse-temurin:17-jdk AS development

WORKDIR /app

# Copiar JAR desde builder
COPY --from=builder /build/target/tournament-management-*.jar app.jar

# Instalar herramientas de desarrollo
RUN apt-get update && apt-get install -y \
    curl \
    vim \
    && rm -rf /var/lib/apt/lists/*

# Crear usuario no root
RUN groupadd -r tournament && useradd -r -g tournament tournament
USER tournament

# Exponer puerto
EXPOSE 8080

# Variables de entorno para desarrollo
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -Dspring.profiles.active=dev"
ENV SPRING_PROFILES_ACTIVE=dev

# Comando de inicio para desarrollo
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ===== TARGET: PRODUCTION =====
FROM eclipse-temurin:17-jre AS production

WORKDIR /app

# Copiar JAR desde builder
COPY --from=builder /build/target/tournament-management-*.jar app.jar

# Crear directorios necesarios
RUN mkdir -p /app/logs /app/backup

# Crear usuario no root
RUN groupadd -r tournament && useradd -r -g tournament tournament
RUN chown -R tournament:tournament /app
USER tournament

# Exponer puerto
EXPOSE 8080

# Variables de entorno para producción
ENV JAVA_OPTS="-Xms1024m -Xmx2048m -XX:+UseG1GC -XX:+UseContainerSupport -Dspring.profiles.active=prod"
ENV SPRING_PROFILES_ACTIVE=prod

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio para producción
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 