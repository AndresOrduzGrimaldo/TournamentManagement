package com.tournament.infrastructure.repository;

import com.tournament.domain.entity.Tournament;
import com.tournament.domain.entity.Category;
import com.tournament.domain.entity.GameType;
import com.tournament.domain.entity.User;
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
        // Crear datos de prueba
        testCategory = new Category();
        testCategory.setName("FPS");
        testCategory.setDescription("First Person Shooter");
        testCategory = entityManager.persistAndFlush(testCategory);

        testGameType = new GameType();
        testGameType.setName("Counter-Strike 2");
        testGameType.setDescription("Competitive FPS game");
        testGameType = entityManager.persistAndFlush(testGameType);

        testOrganizer = new User();
        testOrganizer.setUsername("organizer1");
        testOrganizer.setEmail("organizer@test.com");
        testOrganizer.setPassword("password");
        testOrganizer.setFirstName("Organizador");
        testOrganizer.setLastName("Test");
        testOrganizer.setRole("ORGANIZER");
        testOrganizer = entityManager.persistAndFlush(testOrganizer);

        testTournament = new Tournament();
        testTournament.setName("Torneo de Prueba");
        testTournament.setDescription("Torneo para pruebas unitarias");
        testTournament.setCategory(testCategory);
        testTournament.setGameType(testGameType);
        testTournament.setOrganizer(testOrganizer);
        testTournament.setIsFree(false);
        testTournament.setPrice(new BigDecimal("50.00"));
        testTournament.setMaxParticipants(32);
        testTournament.setCurrentParticipants(0);
        testTournament.setStartDate(LocalDateTime.now().plusDays(7));
        testTournament.setEndDate(LocalDateTime.now().plusDays(14));
        testTournament.setStatus(Tournament.TournamentStatus.PUBLISHED);
        testTournament.setCommissionPercentage(new BigDecimal("5.00"));
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

        Tournament secondTournament = new Tournament();
        secondTournament.setName("Segundo Torneo");
        secondTournament.setDescription("Otro torneo de prueba");
        secondTournament.setCategory(testCategory);
        secondTournament.setGameType(testGameType);
        secondTournament.setOrganizer(testOrganizer);
        secondTournament.setIsFree(true);
        secondTournament.setMaxParticipants(16);
        secondTournament.setCurrentParticipants(0);
        secondTournament.setStartDate(LocalDateTime.now().plusDays(10));
        secondTournament.setEndDate(LocalDateTime.now().plusDays(17));
        secondTournament.setStatus(Tournament.TournamentStatus.PUBLISHED);
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

        Tournament draftTournament = new Tournament();
        draftTournament.setName("Torneo Borrador");
        draftTournament.setDescription("Torneo en borrador");
        draftTournament.setCategory(testCategory);
        draftTournament.setGameType(testGameType);
        draftTournament.setOrganizer(testOrganizer);
        draftTournament.setIsFree(true);
        draftTournament.setMaxParticipants(8);
        draftTournament.setCurrentParticipants(0);
        draftTournament.setStartDate(LocalDateTime.now().plusDays(20));
        draftTournament.setEndDate(LocalDateTime.now().plusDays(27));
        draftTournament.setStatus(Tournament.TournamentStatus.DRAFT);
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