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
        // Configurar datos de prueba
        testCategory = Category.builder()
                .id(1L)
                .name("FPS")
                .description("First Person Shooter")
                .build();

        testGameType = GameType.builder()
                .id(1L)
                .name("Counter-Strike 2")
                .description("Competitive FPS game")
                .build();

        testOrganizer = User.builder()
                .id(1L)
                .username("organizer1")
                .email("organizer@test.com")
                .firstName("Organizador")
                .lastName("Test")
                .role("ORGANIZER")
                .build();

        testTournament = Tournament.builder()
                .id(1L)
                .name("Torneo de Prueba")
                .description("Torneo para pruebas unitarias")
                .category(testCategory)
                .gameType(testGameType)
                .organizer(testOrganizer)
                .isFree(false)
                .price(new BigDecimal("50.00"))
                .maxParticipants(32)
                .currentParticipants(0)
                .startDate(LocalDateTime.now().plusDays(7))
                .endDate(LocalDateTime.now().plusDays(14))
                .status(Tournament.TournamentStatus.PUBLISHED)
                .commissionPercentage(new BigDecimal("5.00"))
                .build();

        testRequest = CreateTournamentRequest.builder()
                .name("Nuevo Torneo")
                .description("Descripción del nuevo torneo")
                .categoryId(1L)
                .gameTypeId(1L)
                .organizerId(1L)
                .isFree(false)
                .price(new BigDecimal("25.00"))
                .maxParticipants(16)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(17))
                .commissionPercentage(new BigDecimal("3.00"))
                .build();
    }

    @Test
    void testCreateTournament_Success() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.of(testGameType));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOrganizer));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(testTournament);

        // Act
        TournamentResponse result = tournamentService.createTournament(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testTournament.getName(), result.getName());
        assertEquals(testTournament.getDescription(), result.getDescription());
        assertEquals(testTournament.getMaxParticipants(), result.getMaxParticipants());
        assertEquals(testTournament.getPrice(), result.getPrice());
        assertEquals(testTournament.getStatus().name(), result.getStatus());

        verify(categoryRepository).findById(1L);
        verify(gameTypeRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void testCreateTournament_CategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.createTournament(testRequest);
        });

        verify(categoryRepository).findById(1L);
        verify(gameTypeRepository, never()).findById(any());
        verify(userRepository, never()).findById(any());
        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void testCreateTournament_GameTypeNotFound() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.createTournament(testRequest);
        });

        verify(categoryRepository).findById(1L);
        verify(gameTypeRepository).findById(1L);
        verify(userRepository, never()).findById(any());
        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void testCreateTournament_OrganizerNotFound() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.of(testGameType));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.createTournament(testRequest);
        });

        verify(categoryRepository).findById(1L);
        verify(gameTypeRepository).findById(1L);
        verify(userRepository).findById(1L);
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
        assertEquals(testTournament.getName(), result.get(0).getName());

        verify(tournamentRepository).findAll();
    }

    @Test
    void testGetTournamentById_Success() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(testTournament));

        // Act
        TournamentResponse result = tournamentService.getTournamentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testTournament.getName(), result.getName());
        assertEquals(testTournament.getDescription(), result.getDescription());

        verify(tournamentRepository).findById(1L);
    }

    @Test
    void testGetTournamentById_NotFound() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.getTournamentById(1L);
        });

        verify(tournamentRepository).findById(1L);
    }

    @Test
    void testUpdateTournament_Success() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(testTournament));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.of(testGameType));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testOrganizer));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(testTournament);

        // Act
        TournamentResponse result = tournamentService.updateTournament(1L, testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testTournament.getName(), result.getName());

        verify(tournamentRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(gameTypeRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void testUpdateTournament_NotFound() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.updateTournament(1L, testRequest);
        });

        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void testDeleteTournament_Success() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(testTournament));
        doNothing().when(tournamentRepository).delete(testTournament);

        // Act
        tournamentService.deleteTournament(1L);

        // Assert
        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository).delete(testTournament);
    }

    @Test
    void testDeleteTournament_NotFound() {
        // Arrange
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tournamentService.deleteTournament(1L);
        });

        verify(tournamentRepository).findById(1L);
        verify(tournamentRepository, never()).delete(any());
    }

    @Test
    void testGetTournamentsByStatus_Success() {
        // Arrange
        List<Tournament> tournaments = Arrays.asList(testTournament);
        when(tournamentRepository.findByStatus(Tournament.TournamentStatus.PUBLISHED)).thenReturn(tournaments);

        // Act
        List<TournamentResponse> result = tournamentService.getTournamentsByStatus("PUBLISHED");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("PUBLISHED", result.get(0).getStatus());

        verify(tournamentRepository).findByStatus(Tournament.TournamentStatus.PUBLISHED);
    }
} 