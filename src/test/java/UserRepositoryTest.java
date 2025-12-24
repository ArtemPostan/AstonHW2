import org.example.models.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    private UserRepository userRepository;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        System.setProperty("hibernate.connection.username", postgres.getUsername());
        System.setProperty("hibernate.connection.password", postgres.getPassword());

    }

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
    }

    @Test
    @DisplayName("Должен успешно сохранить и найти пользователя")
    void shouldSaveAndFindUser() {
        // Given
        User user = User.builder()
                .name("Ivan")
                .email("ivan@example.com")
                .age(25)
                .build();

        // When
        userRepository.save(user);
        User found = userRepository.findById(user.getId());

        // Then
        assertNotNull(found);
        assertEquals("Ivan", found.getName());
        assertEquals("ivan@example.com", found.getEmail());
    }

    @Test
    @DisplayName("Должен выбросить исключение при дубликате Email")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        User user1 = User.builder().name("U1").email("same@test.com").age(20).build();
        User user2 = User.builder().name("U2").email("same@test.com").age(30).build();

        userRepository.save(user1);
        assertThrows(org.example.exception.DataIntegrityViolationException.class, () -> {
            userRepository.save(user2);
        });
    }
}