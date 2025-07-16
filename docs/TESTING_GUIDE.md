# Guía de Pruebas Unitarias - Tournament Management

## Descripción

Esta guía documenta la implementación de pruebas unitarias para la plataforma de gestión de torneos de videojuegos usando JUnit 5 y Mockito.

## Configuración de Pruebas

### Dependencias

Las siguientes dependencias han sido agregadas al `pom.xml`:

```xml
<!-- Testing Dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

### Configuración de Base de Datos de Pruebas

Archivo: `src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
  h2:
    console:
      enabled: true
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

jwt:
  secret: testSecretKeyForTestingPurposesOnly12345678901234567890
  expiration: 86400000

logging:
  level:
    com.tournament: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

## Estructura de Pruebas Implementadas

### 1. Pruebas de Integración de la Aplicación

**Archivo:** `src/test/java/com/tournament/TournamentManagementApplicationTests.java`

```java
@SpringBootTest
@ActiveProfiles("test")
class TournamentManagementApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring se carga correctamente
    }

    @Test
    void testApplicationStarts() {
        // Verifica que la aplicación puede iniciarse
        assert true;
    }
}
```

### 2. Pruebas de Repositorio

**Archivo:** `src/test/java/com/tournament/infrastructure/repository/TournamentRepositoryTest.java`

Pruebas que utilizan `@DataJpaTest` para probar la persistencia de datos:

- `testSaveTournament()`: Verifica que se puede guardar un torneo
- `testFindById()`: Verifica la búsqueda por ID
- `testFindAll()`: Verifica la recuperación de todos los torneos
- `testFindByStatus()`: Verifica filtrado por estado
- `testDeleteTournament()`: Verifica la eliminación de torneos

### 3. Pruebas de Servicios

#### AuthServiceTest

**Archivo:** `src/test/java/com/tournament/application/service/AuthServiceTest.java`

Pruebas unitarias para el servicio de autenticación:

- `testLogin_Success()`: Login exitoso
- `testLogin_UserNotFound()`: Usuario no encontrado
- `testLogin_InvalidPassword()`: Contraseña incorrecta
- `testRegister_Success()`: Registro exitoso
- `testRegister_UsernameAlreadyExists()`: Usuario ya existe
- `testRegister_EmailAlreadyExists()`: Email ya existe
- `testValidateToken_ValidToken()`: Token válido
- `testValidateToken_InvalidToken()`: Token inválido
- `testValidateToken_UserNotFound()`: Usuario no encontrado

#### TournamentServiceTest

**Archivo:** `src/test/java/com/tournament/application/service/TournamentServiceTest.java`

Pruebas unitarias para el servicio de torneos:

- `testCreateTournament_Success()`: Creación exitosa
- `testCreateTournament_CategoryNotFound()`: Categoría no encontrada
- `testCreateTournament_GameTypeNotFound()`: Tipo de juego no encontrado
- `testCreateTournament_OrganizerNotFound()`: Organizador no encontrado
- `testGetAllTournaments_Success()`: Obtener todos los torneos
- `testGetTournamentById_Success()`: Obtener torneo por ID
- `testGetTournamentById_NotFound()`: Torneo no encontrado
- `testUpdateTournament_Success()`: Actualización exitosa
- `testUpdateTournament_NotFound()`: Torneo no encontrado para actualizar
- `testDeleteTournament_Success()`: Eliminación exitosa
- `testDeleteTournament_NotFound()`: Torneo no encontrado para eliminar
- `testGetTournamentsByStatus_Success()`: Filtrado por estado

### 4. Pruebas de Seguridad

#### JwtTokenProviderTest

**Archivo:** `src/test/java/com/tournament/infrastructure/security/JwtTokenProviderTest.java`

Pruebas para el proveedor de tokens JWT:

- `testGenerateToken_Success()`: Generación exitosa de token
- `testValidateToken_ValidToken()`: Validación de token válido
- `testValidateToken_InvalidToken()`: Validación de token inválido
- `testValidateToken_EmptyToken()`: Token vacío
- `testValidateToken_NullToken()`: Token nulo
- `testGetUsernameFromToken_ValidToken()`: Extracción de username
- `testGetUsernameFromToken_InvalidToken()`: Token inválido para extracción
- `testTokenExpiration()`: Expiración de token

### 5. Pruebas de Controladores

#### AuthControllerTest

**Archivo:** `src/test/java/com/tournament/presentation/controller/AuthControllerTest.java`

Pruebas de integración para controladores usando `@WebMvcTest`:

- `testLogin_Success()`: Login exitoso via HTTP
- `testLogin_InvalidRequest()`: Request inválido
- `testRegister_Success()`: Registro exitoso via HTTP
- `testRegister_InvalidRequest()`: Request de registro inválido
- `testValidateToken_Success()`: Validación de token via HTTP
- `testValidateToken_InvalidToken()`: Token inválido via HTTP

## Ejecución de Pruebas

### Ejecutar Todas las Pruebas

```bash
mvn test
```

### Ejecutar Pruebas Específicas

```bash
# Pruebas de integración
mvn test -Dtest=TournamentManagementApplicationTests

# Pruebas de repositorio
mvn test -Dtest=TournamentRepositoryTest

# Pruebas de servicios
mvn test -Dtest=AuthServiceTest
mvn test -Dtest=TournamentServiceTest

# Pruebas de seguridad
mvn test -Dtest=JwtTokenProviderTest

# Pruebas de controladores
mvn test -Dtest=AuthControllerTest
```

### Ejecutar Pruebas con Cobertura

```bash
mvn clean test jacoco:report
```

El reporte de cobertura se genera en: `target/site/jacoco/index.html`

## Problemas Conocidos y Pendientes

### 1. Errores de Compilación

Actualmente existen errores de compilación en las siguientes áreas:

- **Entidades del Dominio**: Faltan getters/setters generados por Lombok
- **Servicios**: Referencias a métodos que no existen en las entidades
- **DTOs**: Faltan métodos builder() en algunas clases
- **Logging**: Variables `log` no definidas en algunos servicios

### 2. Correcciones Necesarias

#### Entidades del Dominio

```java
// Agregar anotaciones Lombok faltantes
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    // ... campos existentes
}
```

#### Servicios

```java
// Agregar logging
@Slf4j
public class TournamentService {
    // ... código existente
}
```

#### DTOs

```java
// Agregar métodos builder() faltantes
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    // ... campos existentes
}
```

### 3. Pruebas Pendientes

- Pruebas de integración completas para todos los controladores
- Pruebas de servicios de tickets
- Pruebas de validación de datos
- Pruebas de manejo de excepciones
- Pruebas de seguridad más completas
- Pruebas de rendimiento

## Mejores Prácticas Implementadas

### 1. Organización de Pruebas

- **Arrange-Act-Assert**: Patrón AAA para estructura de pruebas
- **Nombres Descriptivos**: Nombres de métodos que describen el escenario
- **Separación de Responsabilidades**: Cada prueba verifica una funcionalidad específica

### 2. Configuración de Pruebas

- **Base de Datos en Memoria**: H2 para pruebas rápidas
- **Perfil de Pruebas**: `@ActiveProfiles("test")`
- **Configuración Aislada**: Cada prueba es independiente

### 3. Mocking

- **Mockito**: Para simular dependencias externas
- **@MockBean**: Para beans de Spring en pruebas de controladores
- **@InjectMocks**: Para inyección de mocks en servicios

### 4. Assertions

- **JUnit 5**: Assertions modernas y expresivas
- **Verificación de Comportamiento**: `verify()` para verificar llamadas a mocks
- **Manejo de Excepciones**: `assertThrows()` para excepciones esperadas

## Próximos Pasos

1. **Corregir Errores de Compilación**: Resolver problemas en entidades y servicios
2. **Completar Pruebas**: Implementar pruebas faltantes
3. **Pruebas de Integración**: Agregar pruebas end-to-end
4. **Cobertura de Código**: Alcanzar al menos 80% de cobertura
5. **Pruebas de Rendimiento**: Implementar pruebas de carga
6. **CI/CD**: Integrar pruebas en pipeline de despliegue

## Recursos Adicionales

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-testing)
- [H2 Database](http://www.h2database.com/html/main.html) 