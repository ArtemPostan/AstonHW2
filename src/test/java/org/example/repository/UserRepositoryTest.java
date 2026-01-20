package org.example.repository;

import org.example.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // Добавляем
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager; // Помогает управлять сессией Hibernate

    @Test
    @DisplayName("Должен успешно сохранить и найти пользователя")
    void shouldSaveAndFindUser() {
        // Given
        User user = User.builder()
                .name("Ivan")
                .email("unique_ivan@example.com")
                .age(25)
                .build();

        // When
        userRepository.save(user);

        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findById(user.getId()).orElse(null);

        // Then
        assertNotNull(found);
        assertEquals("Ivan", found.getName());
    }

    @Test
    @DisplayName("Должен выбросить исключение при дубликате Email")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        String duplicateEmail = "double@test.com";
        User user1 = User.builder().name("U1").email(duplicateEmail).age(20).build();
        User user2 = User.builder().name("U2").email(duplicateEmail).age(30).build();

        userRepository.saveAndFlush(user1); // Сохраняем первого сразу

        // When & Then
        entityManager.clear();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(user2);
        });
    }

    @Test
    @DisplayName("Должен обновить имя")
    void shouldUpdateOnlyName() {
        // Given
        User user = User.builder().name("Old").email("update@test.com").age(30).build();
        userRepository.saveAndFlush(user);
        entityManager.clear();

        // When
        User toUpdate = userRepository.findById(user.getId()).orElseThrow();
        toUpdate.setName("New Name");
        userRepository.saveAndFlush(toUpdate);
        entityManager.clear();

        // Then
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("New Name", updated.getName());
        assertEquals("update@test.com", updated.getEmail());
    }

    @Test
    @DisplayName("Должен подтвердить существование email")
    void shouldCheckExistsByEmail() {
        String email = "exists@test.com";
        userRepository.saveAndFlush(User.builder().name("Test").email(email).build());

        boolean exists = userRepository.existsByEmail(email);
        boolean notExists = userRepository.existsByEmail("wrong@test.com");

        assertTrue(exists);
        assertFalse(notExists);
    }
}