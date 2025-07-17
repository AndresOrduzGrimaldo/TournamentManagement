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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = "spring.flyway.enabled=false")
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
        // Crear datos de prueba usando ReflectionTestUtils
        testCategory = new Category();
        ReflectionTestUtils.setField(testCategory, "code", "FPS");
        ReflectionTestUtils.setField(testCategory, "description", "First Person Shooter");
        ReflectionTestUtils.setField(testCategory, "alias", "fps");
        ReflectionTestUtils.setField(testCategory, "isActive", true);
        ReflectionTestUtils.setField(testCategory, "createdAt", java.time.LocalDateTime.now());
        testCategory = entityManager.persistAndFlush(testCategory);

        testGameType = new GameType();
        ReflectionTestUtils.setField(testGameType, "code", "CS2");
        ReflectionTestUtils.setField(testGameType, "fullName", "Counter-Strike 2");
        ReflectionTestUtils.setField(testGameType, "playersCount", 5);
        ReflectionTestUtils.setField(testGameType, "category", testCategory);
        ReflectionTestUtils.setField(testGameType, "isActive", true);
        ReflectionTestUtils.setField(testGameType, "createdAt", java.time.LocalDateTime.now());
        testGameType = entityManager.persistAndFlush(testGameType);

        testOrganizer = new User();
        ReflectionTestUtils.setField(testOrganizer, "username", "organizer1");
        ReflectionTestUtils.setField(testOrganizer, "email", "organizer@test.com");
        ReflectionTestUtils.setField(testOrganizer, "passwordHash", "password");
        ReflectionTestUtils.setField(testOrganizer, "firstName", "Organizador");
        ReflectionTestUtils.setField(testOrganizer, "lastName", "Test");
        ReflectionTestUtils.setField(testOrganizer, "role", User.UserRole.SUBADMIN);
        ReflectionTestUtils.setField(testOrganizer, "isActive", true);
        ReflectionTestUtils.setField(testOrganizer, "createdAt", java.time.LocalDateTime.now());
        testOrganizer = entityManager.persistAndFlush(testOrganizer);

        testTournament = new Tournament();
        ReflectionTestUtils.setField(testTournament, "name", "Torneo de Prueba");
        ReflectionTestUtils.setField(testTournament, "description", "Torneo para pruebas unitarias");
        ReflectionTestUtils.setField(testTournament, "category", testCategory);
        ReflectionTestUtils.setField(testTournament, "gameType", testGameType);
        ReflectionTestUtils.setField(testTournament, "organizer", testOrganizer);
        ReflectionTestUtils.setField(testTournament, "isFree", false);
        ReflectionTestUtils.setField(testTournament, "price", new java.math.BigDecimal("50.00"));
        ReflectionTestUtils.setField(testTournament, "maxParticipants", 32);
        ReflectionTestUtils.setField(testTournament, "currentParticipants", 0);
        ReflectionTestUtils.setField(testTournament, "startDate", java.time.LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(testTournament, "endDate", java.time.LocalDateTime.now().plusDays(14));
        ReflectionTestUtils.setField(testTournament, "status", Tournament.TournamentStatus.PUBLISHED);
        ReflectionTestUtils.setField(testTournament, "commissionPercentage", new java.math.BigDecimal("5.00"));
        ReflectionTestUtils.setField(testTournament, "createdAt", java.time.LocalDateTime.now());
    }

    @Test
    void testSaveTournament() {
        // Act
        Tournament savedTournament = tournamentRepository.save(testTournament);

        // Assert
        assertNotNull(ReflectionTestUtils.getField(savedTournament, "id"));
        assertEquals("Torneo de Prueba", ReflectionTestUtils.getField(savedTournament, "name"));
        assertEquals(ReflectionTestUtils.getField(testCategory, "id"), ReflectionTestUtils.getField(ReflectionTestUtils.getField(savedTournament, "category"), "id"));
        assertEquals(ReflectionTestUtils.getField(testGameType, "id"), ReflectionTestUtils.getField(ReflectionTestUtils.getField(savedTournament, "gameType"), "id"));
        assertEquals(ReflectionTestUtils.getField(testOrganizer, "id"), ReflectionTestUtils.getField(ReflectionTestUtils.getField(savedTournament, "organizer"), "id"));
    }

    @Test
    void testFindById() {
        // Arrange
        Tournament savedTournament = entityManager.persistAndFlush(testTournament);

        // Act
        Optional<Tournament> found = tournamentRepository.findById((Long) ReflectionTestUtils.getField(savedTournament, "id"));

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Torneo de Prueba", ReflectionTestUtils.getField(found.get(), "name"));
    }

    @Test
    void testFindAll() {
        // Arrange
        entityManager.persistAndFlush(testTournament);

        Tournament secondTournament = new Tournament();
        ReflectionTestUtils.setField(secondTournament, "name", "Segundo Torneo");
        ReflectionTestUtils.setField(secondTournament, "description", "Otro torneo de prueba");
        ReflectionTestUtils.setField(secondTournament, "category", testCategory);
        ReflectionTestUtils.setField(secondTournament, "gameType", testGameType);
        ReflectionTestUtils.setField(secondTournament, "organizer", testOrganizer);
        ReflectionTestUtils.setField(secondTournament, "isFree", true);
        ReflectionTestUtils.setField(secondTournament, "maxParticipants", 16);
        ReflectionTestUtils.setField(secondTournament, "currentParticipants", 0);
        ReflectionTestUtils.setField(secondTournament, "startDate", java.time.LocalDateTime.now().plusDays(10));
        ReflectionTestUtils.setField(secondTournament, "endDate", java.time.LocalDateTime.now().plusDays(17));
        ReflectionTestUtils.setField(secondTournament, "status", Tournament.TournamentStatus.PUBLISHED);
        ReflectionTestUtils.setField(secondTournament, "createdAt", java.time.LocalDateTime.now());
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
        ReflectionTestUtils.setField(draftTournament, "name", "Torneo Borrador");
        ReflectionTestUtils.setField(draftTournament, "description", "Torneo en borrador");
        ReflectionTestUtils.setField(draftTournament, "category", testCategory);
        ReflectionTestUtils.setField(draftTournament, "gameType", testGameType);
        ReflectionTestUtils.setField(draftTournament, "organizer", testOrganizer);
        ReflectionTestUtils.setField(draftTournament, "isFree", true);
        ReflectionTestUtils.setField(draftTournament, "maxParticipants", 8);
        ReflectionTestUtils.setField(draftTournament, "currentParticipants", 0);
        ReflectionTestUtils.setField(draftTournament, "startDate", java.time.LocalDateTime.now().plusDays(20));
        ReflectionTestUtils.setField(draftTournament, "endDate", java.time.LocalDateTime.now().plusDays(27));
        ReflectionTestUtils.setField(draftTournament, "status", Tournament.TournamentStatus.DRAFT);
        ReflectionTestUtils.setField(draftTournament, "createdAt", java.time.LocalDateTime.now());
        entityManager.persistAndFlush(draftTournament);

        // Act
        List<Tournament> publishedTournaments = tournamentRepository.findByStatus(Tournament.TournamentStatus.PUBLISHED);

        // Assert
        assertEquals(1, publishedTournaments.size());
        assertEquals("Torneo de Prueba", ReflectionTestUtils.getField(publishedTournaments.get(0), "name"));
    }

    @Test
    void testDeleteTournament() {
        // Arrange
        Tournament savedTournament = entityManager.persistAndFlush(testTournament);

        // Act
        tournamentRepository.delete(savedTournament);

        // Assert
        Optional<Tournament> found = tournamentRepository.findById((Long) ReflectionTestUtils.getField(savedTournament, "id"));
        assertFalse(found.isPresent());
    }
} 