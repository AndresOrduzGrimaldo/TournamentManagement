package com.tournament.infrastructure.repository;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.Category;
import com.tournament.domain.entity.GameType;
import com.tournament.domain.entity.User;
import com.tournament.domain.repository.TournamentRepository;
import com.tournament.domain.repository.CategoryRepository;
import com.tournament.domain.repository.GameTypeRepository;
import com.tournament.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TournamentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

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
    private User testOrganizer;
    private Tournament testTournament;

    @BeforeEach
    void setUp() {
        // Crear datos de prueba usando builders
        testCategory = Category.builder()
                .code("FPS")
                .description("First Person Shooter")
                .alias("fps")
                .isActive(true)
                .build();
        testCategory = entityManager.persistAndFlush(testCategory);

        testGameType = GameType.builder()
                .code("CS2")
                .fullName("Counter-Strike 2")
                .playersCount(5)
                .category(testCategory)
                .isActive(true)
                .build();
        testGameType = entityManager.persistAndFlush(testGameType);

        testOrganizer = User.builder()
                .username("organizer1")
                .email("organizer@test.com")
                .passwordHash("password")
                .firstName("Organizador")
                .lastName("Test")
                .role(User.UserRole.SUBADMIN)
                .isActive(true)
                .build();
        testOrganizer = entityManager.persistAndFlush(testOrganizer);

        testTournament = Tournament.builder()
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
    }

    @Test
    void testSaveTournament() {
        // Act
        Tournament savedTournament = tournamentRepository.save(testTournament);

        // Assert
        assertNotNull(savedTournament.getId());
        assertEquals("Torneo de Prueba", savedTournament.getName());
        assertEquals(testCategory.getId(), savedTournament.getCategory().getId());
        assertEquals(testGameType.getId(), savedTournament.getGameType().getId());
        assertEquals(testOrganizer.getId(), savedTournament.getOrganizer().getId());
    }

    @Test
    void testFindById() {
        // Arrange
        Tournament savedTournament = entityManager.persistAndFlush(testTournament);

        // Act
        Optional<Tournament> found = tournamentRepository.findById(savedTournament.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Torneo de Prueba", found.get().getName());
    }

    @Test
    void testFindAll() {
        // Arrange
        entityManager.persistAndFlush(testTournament);

        Tournament secondTournament = Tournament.builder()
                .name("Segundo Torneo")
                .description("Otro torneo de prueba")
                .category(testCategory)
                .gameType(testGameType)
                .organizer(testOrganizer)
                .isFree(true)
                .maxParticipants(16)
                .currentParticipants(0)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(17))
                .status(Tournament.TournamentStatus.PUBLISHED)
                .build();
        entityManager.persistAndFlush(secondTournament);

        // Act
        List<Tournament> tournaments = tournamentRepository.findAll();

        // Assert
        assertEquals(2, tournaments.size());
    }

    @Test
    void testFindByStatus() {
        // Arrange
        entityManager.persistAndFlush(testTournament);

        Tournament draftTournament = Tournament.builder()
                .name("Torneo Borrador")
                .description("Torneo en borrador")
                .category(testCategory)
                .gameType(testGameType)
                .organizer(testOrganizer)
                .isFree(true)
                .maxParticipants(8)
                .currentParticipants(0)
                .startDate(LocalDateTime.now().plusDays(20))
                .endDate(LocalDateTime.now().plusDays(27))
                .status(Tournament.TournamentStatus.DRAFT)
                .build();
        entityManager.persistAndFlush(draftTournament);

        // Act
        List<Tournament> publishedTournaments = tournamentRepository.findByStatus(Tournament.TournamentStatus.PUBLISHED);

        // Assert
        assertEquals(1, publishedTournaments.size());
        assertEquals("Torneo de Prueba", publishedTournaments.get(0).getName());
    }

    @Test
    void testDeleteTournament() {
        // Arrange
        Tournament savedTournament = entityManager.persistAndFlush(testTournament);

        // Act
        tournamentRepository.delete(savedTournament);

        // Assert
        Optional<Tournament> found = tournamentRepository.findById(savedTournament.getId());
        assertFalse(found.isPresent());
    }
} 