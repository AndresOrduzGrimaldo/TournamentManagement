package com.tournament.presentation.controller;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para TicketController
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@EnableJpaAuditing
@Import(TestSecurityConfig.class)
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private GameTypeRepository gameTypeRepository;

    @Autowired
    private UserRepository userRepository;

    private Category testCategory;
    private GameType testGameType;
    private User testUser;
    private User testOrganizer;
    private Tournament testTournament;
    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        // Limpiar base de datos
        ticketRepository.deleteAll();
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

        // Crear usuario
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "username", "testuser");
        ReflectionTestUtils.setField(testUser, "email", "test@example.com");
        ReflectionTestUtils.setField(testUser, "passwordHash", "$2a$10$xJ6wyYCWnXdBJCX2fL2h.u0R7EqTB.nSyg3liLw0J4Br/cVpXzZRS");
        ReflectionTestUtils.setField(testUser, "firstName", "Test");
        ReflectionTestUtils.setField(testUser, "lastName", "User");
        ReflectionTestUtils.setField(testUser, "role", User.UserRole.PARTICIPANT);
        ReflectionTestUtils.setField(testUser, "isActive", true);
        ReflectionTestUtils.setField(testUser, "createdAt", LocalDateTime.now());
        testUser = userRepository.save(testUser);

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

        // Crear torneo
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

        // Crear ticket
        testTicket = new Ticket();
        ReflectionTestUtils.setField(testTicket, "user", testUser);
        ReflectionTestUtils.setField(testTicket, "tournament", testTournament);
        ReflectionTestUtils.setField(testTicket, "price", BigDecimal.valueOf(10.0));
        ReflectionTestUtils.setField(testTicket, "serviceFee", BigDecimal.valueOf(1.0));
        ReflectionTestUtils.setField(testTicket, "totalAmount", BigDecimal.valueOf(11.0));
        ReflectionTestUtils.setField(testTicket, "status", Ticket.TicketStatus.ACTIVE);
        ReflectionTestUtils.setField(testTicket, "uniqueCode", "TICKET-001");
        ReflectionTestUtils.setField(testTicket, "qrCode", "QR-CODE-001");
        ReflectionTestUtils.setField(testTicket, "purchaseDate", LocalDateTime.now());
        ReflectionTestUtils.setField(testTicket, "createdAt", LocalDateTime.now());
        testTicket = ticketRepository.save(testTicket);
    }

    @Test
    void testCreateTicket_Success() throws Exception {
        // Act & Assert
        Long userId = (Long) ReflectionTestUtils.getField(testUser, "id");
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        
        mockMvc.perform(post("/tickets")
                .param("userId", userId.toString())
                .param("tournamentId", tournamentId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id").value(userId))
                .andExpect(jsonPath("$.tournament.id").value(tournamentId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testCreateTicket_InvalidUser() throws Exception {
        // Act & Assert
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        
        mockMvc.perform(post("/tickets")
                .param("userId", "999")
                .param("tournamentId", tournamentId.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTicket_InvalidTournament() throws Exception {
        // Act & Assert
        Long userId = (Long) ReflectionTestUtils.getField(testUser, "id");
        
        mockMvc.perform(post("/tickets")
                .param("userId", userId.toString())
                .param("tournamentId", "999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetTicket_Success() throws Exception {
        // Act & Assert
        Long ticketId = (Long) ReflectionTestUtils.getField(testTicket, "id");
        
        mockMvc.perform(get("/tickets/{id}", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ticketId))
                .andExpect(jsonPath("$.uniqueCode").value("TICKET-001"))
                .andExpect(jsonPath("$.qrCode").value("QR-CODE-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testGetTicket_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/tickets/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTicketByQR_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/tickets/qr/{qrCode}", "QR-CODE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uniqueCode").value("TICKET-001"))
                .andExpect(jsonPath("$.qrCode").value("QR-CODE-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testGetTicketByQR_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/tickets/qr/{qrCode}", "INVALID-QR"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetTicketsByUser_Success() throws Exception {
        // Act & Assert
        Long userId = (Long) ReflectionTestUtils.getField(testUser, "id");
        
        mockMvc.perform(get("/tickets/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].uniqueCode").value("TICKET-001"))
                .andExpect(jsonPath("$[0].user.id").value(userId));
    }

    @Test
    void testGetTicketsByTournament_Success() throws Exception {
        // Act & Assert
        Long tournamentId = (Long) ReflectionTestUtils.getField(testTournament, "id");
        
        mockMvc.perform(get("/tickets/tournament/{tournamentId}", tournamentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].uniqueCode").value("TICKET-001"))
                .andExpect(jsonPath("$[0].tournament.id").value(tournamentId));
    }

    @Test
    void testValidateTicket_Success() throws Exception {
        // Arrange - Cambiar el torneo a estado IN_PROGRESS y ajustar fechas para que esté en progreso
        ReflectionTestUtils.setField(testTournament, "status", Tournament.TournamentStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(testTournament, "startDate", LocalDateTime.now().minusHours(1)); // Comenzó hace 1 hora
        ReflectionTestUtils.setField(testTournament, "endDate", LocalDateTime.now().plusHours(2)); // Termina en 2 horas
        tournamentRepository.save(testTournament);

        // Act & Assert
        mockMvc.perform(post("/tickets/validate")
                .param("qrCode", "QR-CODE-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.message").value("Ticket validado exitosamente"));
    }

    @Test
    void testValidateTicket_InvalidQR() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/tickets/validate")
                .param("qrCode", "INVALID-QR"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Ticket no válido o ya usado"));
    }

    @Test
    void testCancelTicket_Success() throws Exception {
        // Act & Assert
        Long ticketId = (Long) ReflectionTestUtils.getField(testTicket, "id");
        
        mockMvc.perform(post("/tickets/{id}/cancel", ticketId))
                .andExpect(status().isOk());
    }

    @Test
    void testCancelTicket_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/tickets/{id}/cancel", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCancelTicket_AlreadyUsed() throws Exception {
        // Arrange - Marcar ticket como usado
        ReflectionTestUtils.setField(testTicket, "status", Ticket.TicketStatus.USED);
        ReflectionTestUtils.setField(testTicket, "usedAt", LocalDateTime.now());
        ticketRepository.save(testTicket);

        // Act & Assert
        Long ticketId = (Long) ReflectionTestUtils.getField(testTicket, "id");
        
        mockMvc.perform(post("/tickets/{id}/cancel", ticketId))
                .andExpect(status().isConflict());
    }

    @Test
    void testGenerateQRImage_Success() throws Exception {
        // Act & Assert
        Long ticketId = (Long) ReflectionTestUtils.getField(testTicket, "id");
        
        mockMvc.perform(get("/tickets/{id}/qr-image", ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").value("QR-CODE-001"))
                .andExpect(jsonPath("$.qrImage").exists());
    }

    @Test
    void testGenerateQRImage_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/tickets/{id}/qr-image", 999L))
                .andExpect(status().isNotFound());
    }
} 