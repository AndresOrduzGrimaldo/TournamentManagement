package com.tournament.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tournament.application.dto.CreateTournamentRequest;
import com.tournament.application.dto.TournamentResponse;
import com.tournament.domain.entity.*;
import com.tournament.domain.repository.*;
import com.tournament.infrastructure.security.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para TournamentController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@EnableJpaAuditing
@Import(TestSecurityConfig.class)
class TournamentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GameTypeRepository gameTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;
    private GameType testGameType;
    private User testOrganizer;
    private Tournament testTournament;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        tournamentRepository.deleteAll();
        gameTypeRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Crear datos de prueba
        createTestData();
    }

    private void createTestData() {
        // Crear categoría
        testCategory = new Category();
        ReflectionTestUtils.setField(testCategory, "code", "FPS");
        ReflectionTestUtils.setField(testCategory, "description", "First Person Shooter");
        ReflectionTestUtils.setField(testCategory, "alias", "FPS Games");
        ReflectionTestUtils.setField(testCategory, "isActive", true);
        ReflectionTestUtils.setField(testCategory, "createdAt", LocalDateTime.now());
        testCategory = categoryRepository.save(testCategory);

        // Crear tipo de juego
        testGameType = new GameType();
        ReflectionTestUtils.setField(testGameType, "code", "CS2");
        ReflectionTestUtils.setField(testGameType, "fullName", "Counter-Strike 2");
        ReflectionTestUtils.setField(testGameType, "playersCount", 5);
        ReflectionTestUtils.setField(testGameType, "category", testCategory);
        ReflectionTestUtils.setField(testGameType, "isActive", true);
        ReflectionTestUtils.setField(testGameType, "createdAt", LocalDateTime.now());
        testGameType = gameTypeRepository.save(testGameType);

        // Crear organizador
        testOrganizer = new User();
        ReflectionTestUtils.setField(testOrganizer, "username", "organizer");
        ReflectionTestUtils.setField(testOrganizer, "email", "organizer@example.com");
        ReflectionTestUtils.setField(testOrganizer, "passwordHash", "$2a$10$xJ6wyYCWnXdBJCX2fL2h.u0R7EqTB.nSyg3liLw0J4Br/cVpXzZRS");
        ReflectionTestUtils.setField(testOrganizer, "firstName", "Test");
        ReflectionTestUtils.setField(testOrganizer, "lastName", "Organizer");
        ReflectionTestUtils.setField(testOrganizer, "role", User.UserRole.SUBADMIN);
        ReflectionTestUtils.setField(testOrganizer, "isActive", true);
        ReflectionTestUtils.setField(testOrganizer, "createdAt", LocalDateTime.now());
        testOrganizer = userRepository.save(testOrganizer);

        // Crear torneo de prueba
        testTournament = new Tournament();
        ReflectionTestUtils.setField(testTournament, "name", "Test Tournament");
        ReflectionTestUtils.setField(testTournament, "description", "Test tournament description");
        ReflectionTestUtils.setField(testTournament, "category", testCategory);
        ReflectionTestUtils.setField(testTournament, "gameType", testGameType);
        ReflectionTestUtils.setField(testTournament, "organizer", testOrganizer);
        ReflectionTestUtils.setField(testTournament, "isFree", true);
        ReflectionTestUtils.setField(testTournament, "price", BigDecimal.ZERO);
        ReflectionTestUtils.setField(testTournament, "maxParticipants", 10);
        ReflectionTestUtils.setField(testTournament, "currentParticipants", 0);
        ReflectionTestUtils.setField(testTournament, "startDate", LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(testTournament, "endDate", LocalDateTime.now().plusDays(2));
        ReflectionTestUtils.setField(testTournament, "status", Tournament.TournamentStatus.REGISTRATION_OPEN);
        ReflectionTestUtils.setField(testTournament, "commissionPercentage", BigDecimal.valueOf(5.0));
        ReflectionTestUtils.setField(testTournament, "createdAt", LocalDateTime.now());
        testTournament = tournamentRepository.save(testTournament);
    }

    @Test
    void testCreateTournament_Success() throws Exception {
        // Arrange
        CreateTournamentRequest request = new CreateTournamentRequest();
        ReflectionTestUtils.setField(request, "name", "New Tournament");
        ReflectionTestUtils.setField(request, "description", "New tournament description");
        ReflectionTestUtils.setField(request, "categoryId", ReflectionTestUtils.getField(testCategory, "id"));
        ReflectionTestUtils.setField(request, "gameTypeId", ReflectionTestUtils.getField(testGameType, "id"));
        ReflectionTestUtils.setField(request, "organizerId", ReflectionTestUtils.getField(testOrganizer, "id"));
        ReflectionTestUtils.setField(request, "isFree", true);
        ReflectionTestUtils.setField(request, "price", BigDecimal.ZERO);
        ReflectionTestUtils.setField(request, "maxParticipants", 20);
        ReflectionTestUtils.setField(request, "startDate", LocalDateTime.now().plusDays(1));
        ReflectionTestUtils.setField(request, "endDate", LocalDateTime.now().plusDays(2));
        ReflectionTestUtils.setField(request, "commissionPercentage", BigDecimal.valueOf(5.0));

        // Act & Assert
        mockMvc.perform(post("/tournaments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Tournament"))
                .andExpect(jsonPath("$.description").value("New tournament description"))
                .andExpect(jsonPath("$.isFree").value(true))
                .andExpect(jsonPath("$.maxParticipants").value(20))
                .andExpect(jsonPath("$.currentParticipants").value(0))
                .andExpect(jsonPath("$.status").value("DRAFT")); // Los torneos se crean en estado DRAFT
    }

    @Test
    void testCreateTournament_InvalidData() throws Exception {
        // Arrange - Request sin datos obligatorios
        CreateTournamentRequest request = new CreateTournamentRequest();
        ReflectionTestUtils.setField(request, "name", ""); // Nombre vacío
        ReflectionTestUtils.setField(request, "maxParticipants", 1); // Menos de 2 participantes

        // Act & Assert
        mockMvc.perform(post("/tournaments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTournament_Success() throws Exception {
        // Act & Assert
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        mockMvc.perform(get("/tournaments/{id}", tournamentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tournamentId))
                .andExpect(jsonPath("$.name").value("Test Tournament"))
                .andExpect(jsonPath("$.description").value("Test tournament description"))
                .andExpect(jsonPath("$.isFree").value(true))
                .andExpect(jsonPath("$.maxParticipants").value(10))
                .andExpect(jsonPath("$.currentParticipants").value(0))
                .andExpect(jsonPath("$.status").value("REGISTRATION_OPEN"));
    }

    @Test
    void testGetTournament_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/tournaments/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllTournaments_Success() throws Exception {
        // Act & Assert
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        mockMvc.perform(get("/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(tournamentId))
                .andExpect(jsonPath("$[0].name").value("Test Tournament"));
    }

    @Test
    void testGetTournamentsByOrganizer_Success() throws Exception {
        // Act & Assert
        Long organizerId = (Long) ReflectionTestUtils.getField(testOrganizer, "id");
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        mockMvc.perform(get("/tournaments/organizer/{organizerId}", organizerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(tournamentId))
                .andExpect(jsonPath("$[0].name").value("Test Tournament"))
                .andExpect(jsonPath("$[0].organizer.id").value(organizerId));
    }

    @Test
    void testGetOpenTournaments_Success() throws Exception {
        // Act & Assert
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        mockMvc.perform(get("/tournaments/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(tournamentId))
                .andExpect(jsonPath("$[0].status").value("REGISTRATION_OPEN"));
    }

    @Test
    void testUpdateTournamentStatus_Success() throws Exception {
        // Act & Assert
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        mockMvc.perform(put("/tournaments/{id}/status", tournamentId)
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tournamentId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void testUpdateTournamentStatus_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/tournaments/{id}/status", 999L)
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testIncrementParticipants_Success() throws Exception {
        // Act & Assert
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        mockMvc.perform(post("/tournaments/{id}/participants/increment", tournamentId))
                .andExpect(status().isOk());

        // Verificar que se incrementó el contador
        mockMvc.perform(get("/tournaments/{id}", tournamentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentParticipants").value(1));
    }

    @Test
    void testIncrementParticipants_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/tournaments/{id}/participants/increment", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testIncrementParticipants_TournamentFull() throws Exception {
        // Arrange - Llenar el torneo
        ReflectionTestUtils.setField(testTournament, "currentParticipants", 10);
        ReflectionTestUtils.setField(testTournament, "maxParticipants", 10);
        tournamentRepository.save(testTournament);

        // Act & Assert
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        mockMvc.perform(post("/tournaments/{id}/participants/increment", tournamentId))
                .andExpect(status().isConflict());
    }

    @Test
    void testDecrementParticipants_Success() throws Exception {
        // Arrange - Agregar un participante primero
        ReflectionTestUtils.setField(testTournament, "currentParticipants", 1);
        tournamentRepository.save(testTournament);

        // Act & Assert
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        mockMvc.perform(post("/tournaments/{id}/participants/decrement", tournamentId))
                .andExpect(status().isOk());

        // Verificar que se decrementó el contador
        mockMvc.perform(get("/tournaments/{id}", tournamentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentParticipants").value(0));
    }

    @Test
    void testDecrementParticipants_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/tournaments/{id}/participants/decrement", 999L))
                .andExpect(status().isNotFound());
    }
} 