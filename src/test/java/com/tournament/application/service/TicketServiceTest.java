package com.tournament.application.service;

import com.tournament.domain.entity.*;
import com.tournament.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    private User testUser;
    private Tournament testTournament;
    private Ticket testTicket;
    private Category testCategory;
    private GameType testGameType;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba usando reflection para evitar problemas de Lombok
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        ReflectionTestUtils.setField(testUser, "username", "testuser");
        ReflectionTestUtils.setField(testUser, "email", "test@example.com");
        ReflectionTestUtils.setField(testUser, "firstName", "Test");
        ReflectionTestUtils.setField(testUser, "lastName", "User");
        ReflectionTestUtils.setField(testUser, "role", User.UserRole.PARTICIPANT);
        ReflectionTestUtils.setField(testUser, "isActive", true);

        testCategory = new Category();
        ReflectionTestUtils.setField(testCategory, "id", 1L);
        ReflectionTestUtils.setField(testCategory, "code", "FPS");
        ReflectionTestUtils.setField(testCategory, "description", "First Person Shooter");
        ReflectionTestUtils.setField(testCategory, "alias", "FPS Games");
        ReflectionTestUtils.setField(testCategory, "isActive", true);
        ReflectionTestUtils.setField(testCategory, "createdAt", LocalDateTime.now());

        testGameType = new GameType();
        ReflectionTestUtils.setField(testGameType, "id", 1L);
        ReflectionTestUtils.setField(testGameType, "code", "CS");
        ReflectionTestUtils.setField(testGameType, "fullName", "Counter-Strike: Global Offensive");
        ReflectionTestUtils.setField(testGameType, "playersCount", 5);
        ReflectionTestUtils.setField(testGameType, "category", testCategory);
        ReflectionTestUtils.setField(testGameType, "isActive", true);
        ReflectionTestUtils.setField(testGameType, "createdAt", LocalDateTime.now());

        testTournament = new Tournament();
        ReflectionTestUtils.setField(testTournament, "id", 1L);
        ReflectionTestUtils.setField(testTournament, "name", "Test Tournament");
        ReflectionTestUtils.setField(testTournament, "description", "Test tournament description");
        ReflectionTestUtils.setField(testTournament, "category", testCategory);
        ReflectionTestUtils.setField(testTournament, "gameType", testGameType);
        ReflectionTestUtils.setField(testTournament, "organizer", testUser);
        ReflectionTestUtils.setField(testTournament, "status", Tournament.TournamentStatus.REGISTRATION_OPEN);
        ReflectionTestUtils.setField(testTournament, "isFree", false);
        ReflectionTestUtils.setField(testTournament, "price", new BigDecimal("50.00"));
        ReflectionTestUtils.setField(testTournament, "maxParticipants", 100);
        ReflectionTestUtils.setField(testTournament, "currentParticipants", 50);
        ReflectionTestUtils.setField(testTournament, "startDate", LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(testTournament, "endDate", LocalDateTime.now().plusDays(8));
        ReflectionTestUtils.setField(testTournament, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(testTournament, "updatedAt", LocalDateTime.now());

        testTicket = new Ticket();
        ReflectionTestUtils.setField(testTicket, "id", 1L);
        ReflectionTestUtils.setField(testTicket, "user", testUser);
        ReflectionTestUtils.setField(testTicket, "tournament", testTournament);
        ReflectionTestUtils.setField(testTicket, "qrCode", "QR-CODE-001");
        ReflectionTestUtils.setField(testTicket, "uniqueCode", "UNIQUE-001");
        ReflectionTestUtils.setField(testTicket, "purchaseDate", LocalDateTime.now());
        ReflectionTestUtils.setField(testTicket, "price", new BigDecimal("50.00"));
        ReflectionTestUtils.setField(testTicket, "serviceFee", new BigDecimal("5.00"));
        ReflectionTestUtils.setField(testTicket, "totalAmount", new BigDecimal("55.00"));
        ReflectionTestUtils.setField(testTicket, "status", Ticket.TicketStatus.ACTIVE);
        ReflectionTestUtils.setField(testTicket, "usedAt", null);
    }

    @Test
    void testCreateTicket_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(testTournament));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(testTournament);

        // Act
        Ticket result = ticketService.createTicket(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("QR-CODE-001", ReflectionTestUtils.getField(result, "qrCode"));
        assertEquals("UNIQUE-001", ReflectionTestUtils.getField(result, "uniqueCode"));
        assertEquals(Ticket.TicketStatus.ACTIVE, ReflectionTestUtils.getField(result, "status"));

        verify(userRepository).findById(1L);
        verify(tournamentRepository).findById(1L);
        verify(ticketRepository).save(any(Ticket.class));
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void testCreateTicket_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicket(1L, 1L);
        });

        verify(userRepository).findById(1L);
        verify(tournamentRepository, never()).findById(any());
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void testCreateTicket_TournamentNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.createTicket(1L, 1L);
        });

        verify(userRepository).findById(1L);
        verify(tournamentRepository).findById(1L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void testCreateTicket_TournamentNotOpenForRegistration() {
        // Arrange
        ReflectionTestUtils.setField(testTournament, "status", Tournament.TournamentStatus.IN_PROGRESS);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(testTournament));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            ticketService.createTicket(1L, 1L);
        });

        verify(userRepository).findById(1L);
        verify(tournamentRepository).findById(1L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void testCreateTicket_TournamentFull() {
        // Arrange
        ReflectionTestUtils.setField(testTournament, "currentParticipants", 100);
        ReflectionTestUtils.setField(testTournament, "maxParticipants", 100);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(testTournament));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            ticketService.createTicket(1L, 1L);
        });

        verify(userRepository).findById(1L);
        verify(tournamentRepository).findById(1L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void testGetTicketById_Success() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // Act
        Optional<Ticket> result = ticketService.getTicketById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, ReflectionTestUtils.getField(result.get(), "id"));
        verify(ticketRepository).findById(1L);
    }

    @Test
    void testGetTicketById_NotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Ticket> result = ticketService.getTicketById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(ticketRepository).findById(1L);
    }

    @Test
    void testGetTicketByQRCode_Success() {
        // Arrange
        when(ticketRepository.findByQrCode("QR-CODE-001")).thenReturn(Optional.of(testTicket));

        // Act
        Optional<Ticket> result = ticketService.getTicketByQRCode("QR-CODE-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("QR-CODE-001", ReflectionTestUtils.getField(result.get(), "qrCode"));
        verify(ticketRepository).findByQrCode("QR-CODE-001");
    }

    @Test
    void testGetTicketByUniqueCode_Success() {
        // Arrange
        when(ticketRepository.findByUniqueCode("UNIQUE-001")).thenReturn(Optional.of(testTicket));

        // Act
        Optional<Ticket> result = ticketService.getTicketByUniqueCode("UNIQUE-001");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("UNIQUE-001", ReflectionTestUtils.getField(result.get(), "uniqueCode"));
        verify(ticketRepository).findByUniqueCode("UNIQUE-001");
    }

    @Test
    void testGetTicketsByUser_Success() {
        // Arrange
        List<Ticket> tickets = Arrays.asList(testTicket);
        when(ticketRepository.findByUserId(1L)).thenReturn(tickets);

        // Act
        List<Ticket> result = ticketService.getTicketsByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, ReflectionTestUtils.getField(result.get(0), "id"));
        verify(ticketRepository).findByUserId(1L);
    }

    @Test
    void testGetTicketsByTournament_Success() {
        // Arrange
        List<Ticket> tickets = Arrays.asList(testTicket);
        when(ticketRepository.findByTournamentId(1L)).thenReturn(tickets);

        // Act
        List<Ticket> result = ticketService.getTicketsByTournament(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, ReflectionTestUtils.getField(result.get(0), "id"));
        verify(ticketRepository).findByTournamentId(1L);
    }

    @Test
    void testValidateAndUseTicket_Success() {
        // Arrange
        ReflectionTestUtils.setField(testTournament, "status", Tournament.TournamentStatus.IN_PROGRESS);
        ReflectionTestUtils.setField(testTournament, "startDate", LocalDateTime.now().minusHours(1));
        ReflectionTestUtils.setField(testTournament, "endDate", LocalDateTime.now().plusHours(2));
        
        when(ticketRepository.findByQrCode("QR-CODE-001")).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // Act
        boolean result = ticketService.validateAndUseTicket("QR-CODE-001");

        // Assert
        assertTrue(result);
        verify(ticketRepository).findByQrCode("QR-CODE-001");
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void testValidateAndUseTicket_NotFound() {
        // Arrange
        when(ticketRepository.findByQrCode("INVALID-QR")).thenReturn(Optional.empty());

        // Act
        boolean result = ticketService.validateAndUseTicket("INVALID-QR");

        // Assert
        assertFalse(result);
        verify(ticketRepository).findByQrCode("INVALID-QR");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void testValidateAndUseTicket_InvalidTicket() {
        // Arrange
        ReflectionTestUtils.setField(testTicket, "status", Ticket.TicketStatus.CANCELLED);
        when(ticketRepository.findByQrCode("QR-CODE-001")).thenReturn(Optional.of(testTicket));

        // Act
        boolean result = ticketService.validateAndUseTicket("QR-CODE-001");

        // Assert
        assertFalse(result);
        verify(ticketRepository).findByQrCode("QR-CODE-001");
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void testCancelTicket_Success() {
        // Arrange
        ReflectionTestUtils.setField(testTicket, "status", Ticket.TicketStatus.ACTIVE); // Asegurar estado ACTIVE
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(testTournament);

        // Act
        ticketService.cancelTicket(1L);

        // Assert
        verify(ticketRepository).findById(1L);
        verify(ticketRepository).save(any(Ticket.class));
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void testCancelTicket_NotFound() {
        // Arrange
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            ticketService.cancelTicket(1L);
        });

        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void testCancelTicket_AlreadyUsed() {
        // Arrange
        ReflectionTestUtils.setField(testTicket, "status", Ticket.TicketStatus.USED);
        ReflectionTestUtils.setField(testTicket, "usedAt", LocalDateTime.now());
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            ticketService.cancelTicket(1L);
        });

        verify(ticketRepository).findById(1L);
        verify(ticketRepository, never()).save(any());
    }

    @Test
    void testGenerateQRCodeImage_Success() {
        // Arrange
        String qrCode = "QR-CODE-001";

        // Act
        String result = ticketService.generateQRCodeImage(qrCode);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.startsWith("data:image/png;base64,"), "El resultado debe comenzar con 'data:image/png;base64,'");
    }
} 