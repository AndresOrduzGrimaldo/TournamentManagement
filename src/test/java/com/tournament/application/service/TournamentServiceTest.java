package com.tournament.application.service;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.Category;
import com.tournament.domain.entity.GameType;
import com.tournament.domain.entity.User;
import com.tournament.domain.repository.TournamentRepository;
import com.tournament.domain.repository.CategoryRepository;
import com.tournament.domain.repository.GameTypeRepository;
import com.tournament.domain.repository.UserRepository;
import com.tournament.application.dto.CreateTournamentRequest;
import com.tournament.application.dto.TournamentResponse;
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
class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private GameTypeRepository gameTypeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TournamentService tournamentService;

    private Tournament testTournament;
    private Category testCategory;
    private GameType testGameType;
    private User testOrganizer;
    private CreateTournamentRequest testRequest;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba usando ReflectionTestUtils
        testCategory = new Category();
        ReflectionTestUtils.setField(testCategory, "id", 1L);
        ReflectionTestUtils.setField(testCategory, "code", "FPS");
        ReflectionTestUtils.setField(testCategory, "description", "First Person Shooter");
        ReflectionTestUtils.setField(testCategory, "alias", "fps");
        ReflectionTestUtils.setField(testCategory, "isActive", true);

        testGameType = new GameType();
        ReflectionTestUtils.setField(testGameType, "id", 1L);
        ReflectionTestUtils.setField(testGameType, "code", "CS2");
        ReflectionTestUtils.setField(testGameType, "fullName", "Counter-Strike 2");
        ReflectionTestUtils.setField(testGameType, "playersCount", 5);
        ReflectionTestUtils.setField(testGameType, "category", testCategory);
        ReflectionTestUtils.setField(testGameType, "isActive", true);

        testOrganizer = new User();
        ReflectionTestUtils.setField(testOrganizer, "id", 1L);
        ReflectionTestUtils.setField(testOrganizer, "username", "organizer1");
        ReflectionTestUtils.setField(testOrganizer, "email", "organizer@test.com");
        ReflectionTestUtils.setField(testOrganizer, "firstName", "Organizador");
        ReflectionTestUtils.setField(testOrganizer, "lastName", "Test");
        ReflectionTestUtils.setField(testOrganizer, "role", User.UserRole.SUBADMIN);
        ReflectionTestUtils.setField(testOrganizer, "isActive", true);

        testTournament = new Tournament();
        ReflectionTestUtils.setField(testTournament, "id", 1L);
        ReflectionTestUtils.setField(testTournament, "name", "Torneo de Prueba");
        ReflectionTestUtils.setField(testTournament, "description", "Torneo para pruebas unitarias");
        ReflectionTestUtils.setField(testTournament, "category", testCategory);
        ReflectionTestUtils.setField(testTournament, "gameType", testGameType);
        ReflectionTestUtils.setField(testTournament, "organizer", testOrganizer);
        ReflectionTestUtils.setField(testTournament, "isFree", false);
        ReflectionTestUtils.setField(testTournament, "price", new BigDecimal("50.00"));
        ReflectionTestUtils.setField(testTournament, "maxParticipants", 32);
        ReflectionTestUtils.setField(testTournament, "currentParticipants", 0);
        ReflectionTestUtils.setField(testTournament, "startDate", LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(testTournament, "endDate", LocalDateTime.now().plusDays(14));
        ReflectionTestUtils.setField(testTournament, "status", Tournament.TournamentStatus.PUBLISHED);
        ReflectionTestUtils.setField(testTournament, "commissionPercentage", new BigDecimal("5.00"));

        testRequest = new CreateTournamentRequest();
        ReflectionTestUtils.setField(testRequest, "name", "Nuevo Torneo");
        ReflectionTestUtils.setField(testRequest, "description", "Descripción del nuevo torneo");
        ReflectionTestUtils.setField(testRequest, "categoryId", 1L);
        ReflectionTestUtils.setField(testRequest, "gameTypeId", 1L);
        ReflectionTestUtils.setField(testRequest, "organizerId", 1L);
        ReflectionTestUtils.setField(testRequest, "isFree", false);
        ReflectionTestUtils.setField(testRequest, "price", new BigDecimal("25.00"));
        ReflectionTestUtils.setField(testRequest, "maxParticipants", 16);
        ReflectionTestUtils.setField(testRequest, "startDate", LocalDateTime.now().plusDays(10));
        ReflectionTestUtils.setField(testRequest, "endDate", LocalDateTime.now().plusDays(17));
        ReflectionTestUtils.setField(testRequest, "commissionPercentage", new BigDecimal("3.00"));
    }

    @Test
    void testCreateTournament_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOrganizer));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.of(testGameType));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(testTournament);

        // Act
        TournamentResponse result = tournamentService.createTournament(testRequest);

        // Assert
        assertNotNull(result);

        verify(userRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(gameTypeRepository).findById(1L);
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void testCreateTournament_CategoryNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOrganizer));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.createTournament(testRequest);
        });

        verify(userRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(gameTypeRepository, never()).findById(any());
        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void testCreateTournament_GameTypeNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOrganizer));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.createTournament(testRequest);
        });

        verify(userRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(gameTypeRepository).findById(1L);
        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void testCreateTournament_OrganizerNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.createTournament(testRequest);
        });

        verify(userRepository).findById(1L);
        verify(categoryRepository, never()).findById(any());
        verify(gameTypeRepository, never()).findById(any());
        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void testGetAllTournaments_Success() {
        // Arrange
        List<Tournament> tournaments = Arrays.asList(testTournament);
        when(tournamentRepository.findAll()).thenReturn(tournaments);

        // Act
        List<TournamentResponse> result = tournamentService.getAllTournaments();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        verify(tournamentRepository).findAll();
    }

    @Test
    void testGetTournamentById_Success() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(testTournament));

        // Act
        Optional<TournamentResponse> result = tournamentService.getTournamentById(1L);

        // Assert
        assertTrue(result.isPresent());

        verify(tournamentRepository).findById(1L);
    }

    @Test
    void testGetTournamentById_NotFound() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<TournamentResponse> result = tournamentService.getTournamentById(1L);

        // Assert
        assertFalse(result.isPresent());

        verify(tournamentRepository).findById(1L);
    }
} 