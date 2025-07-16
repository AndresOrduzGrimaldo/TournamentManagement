# Dockerfile para la aplicación de gestión de torneos
FROM openjdk:17-jdk-slim

# Etiquetas de metadatos
LABEL maintainer="Tournament Management Team"
LABEL version="1.0.0"
LABEL description="Plataforma de Gestión de Torneos de Videojuegos"

# Crear directorio de trabajo
WORKDIR /app

# Copiar el archivo JAR de la aplicación
COPY target/tournament-management-*.jar app.jar

# Crear usuario no root para seguridad
RUN groupadd -r tournament && useradd -r -g tournament tournament
USER tournament

# Exponer puerto de la aplicación
EXPOSE 8080

# Variables de entorno para configuración
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 