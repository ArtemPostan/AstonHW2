package org.example.repository;

import org.example.models.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @ValueSource(ints = {18, 25, 99})
    @DisplayName("Должен сохранять пользователя с валидным возрастом")
    void shouldSaveWithDifferentAges(int age) {
        User user = User.builder().name("User").email(age + "@test.com").age(age).build();
        userRepository.save(user);
        assertNotNull(user.getId());
    }

    @Test
    @DisplayName("Должен обновить имя и оставить email прежним")
    void shouldUpdateOnlyName() {
        User user = User.builder().name("Old").email("stay@same.com").age(30).build();
        userRepository.save(user);

        user.setName("New Name");
        userRepository.update(user);

        User updated = userRepository.findById(user.getId());
        assertEquals("New Name", updated.getName());
        assertEquals("stay@same.com", updated.getEmail());
    }

    @Test
    @DisplayName("Должен вернуть null при поиске по несуществующему ID")
    void shouldReturnNullWhenUserNotFound() {
        // Given
        long nonExistentId = 999L;

        // When
        User found = userRepository.findById(nonExistentId);

        // Then
        assertNull(found, "Если пользователя нет, должен вернуться null");
    }

    @Test
    @DisplayName("Не должен выбрасывать исключение при удалении несуществующего ID")
    void shouldNotFailWhenDeletingNonExistentUser() {
        // Given
        long id = 555L;

        // When & Then
        assertDoesNotThrow(() -> userRepository.delete(id),
                "Удаление несуществующего пользователя не должно приводить к ошибке");
    }

    @Test
    @DisplayName("Должен выбросить исключение, если email равен null")
    void shouldThrowExceptionWhenEmailIsNull() {
        // Given
        User userWithNullEmail = User.builder()
                .name("NoEmail")
                .email(null)
                .age(30)
                .build();

        // When & Then
        assertThrows(Exception.class, () -> {
            userRepository.save(userWithNullEmail);
        }, "Сохранение без email должно вызвать ошибку на уровне БД или Hibernate");
    }
}