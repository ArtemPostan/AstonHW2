package org.example.services;

import org.example.exception.DataIntegrityViolationException;
import org.example.models.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Должен выбросить исключение, если формат почты неверный")
    void shouldThrowExceptionWhenEmailIsInvalid() {
        // Given
        String invalidEmail = "test@com";

        // When & Then
        assertThrows(DataIntegrityViolationException.class, () ->
                userService.createUser("Ivan", invalidEmail, 20)
        );
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("Должен успешно вызвать сохранение при валидных данных")
    void shouldSaveUserWhenDataIsValid() {
        // Given
        String email = "correct@mail.com";

        // When
        userService.createUser("Ivan", email, 25);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Должен вернуть false при удалении, если пользователь не найден")
    void shouldReturnFalseWhenDeletingNonExistentUser() {
        // Given
        long id = 99L;
        when(userRepository.findById(id)).thenReturn(null);

        // When
        boolean deleted = userService.delete(id);

        // Then
        assertFalse(deleted);
        verify(userRepository, never()).delete(id);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, 200})
    @DisplayName("Должен выбросить исключение при невалидном возрасте")
    void shouldThrowExceptionForInvalidAge(int invalidAge) {
        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.createUser("Test", "test@mail.com", invalidAge);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plainaddress",
            "@example.com",
            "joe smith@test.com",
            "test@com",
            ""
    })
    @DisplayName("Должен отклонять некорректные форматы Email")
     void shouldRejectInvalidEmailFormats(String invalidEmail) {
        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.createUser("Ivan", invalidEmail, 25);
        }, "Сервис должен был отклонить email: " + invalidEmail);
    }

    @Test
    @DisplayName("Должен выбросить DataIntegrityViolationException, если репозиторий вернул ошибку Not-Null constraint")
    void shouldThrowDataIntegrityExceptionWhenRepositoryFails() {
        // Given
        User invalidUser = User.builder()
                .id(1L)
                .name("Artem")
                .email(null) // Это вызовет ошибку
                .age(35)
                .build();

        doThrow(new DataIntegrityViolationException("Поле email обязательно"))
                .when(userRepository).update(any(User.class));

        assertThrows(DataIntegrityViolationException.class, () -> {
            userService.update(invalidUser);
        });
    }

    @Test
    @DisplayName("Данные не должны затираться на null при частичном обновлении")
    void shouldKeepOldDataWhenNewDataIsEmpty() {
        long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .name("Artem")
                .email("kwaka88@mail.ru")
                .age(34)
                .build();

        String inputName = "";  // Enter
        String inputEmail = ""; // Enter
        String inputAge = "35";

        when(userRepository.findById(userId)).thenReturn(existingUser);
        User userFromDb = userService.findById(userId);

        if (!inputName.isBlank()) userFromDb.setName(inputName);
        if (!inputEmail.isBlank()) userFromDb.setEmail(inputEmail);
        if (!inputAge.isBlank()) userFromDb.setAge(Integer.parseInt(inputAge));

        userService.update(userFromDb);

        verify(userRepository).update(argThat(updatedUser ->
                updatedUser.getName().equals("Artem") &&
                        updatedUser.getEmail().equals("kwaka88@mail.ru") &&
                        updatedUser.getAge() == 35
        ));
    }

    @Test
    @DisplayName("Метод update должен сохранить объект с корректными полями при частичном изменении")
    void shouldUpdateUserWithMixedData() {

        User existingUser = User.builder()
                .id(1L)
                .name("Artem")
                .email("kwaka88@mail.ru")
                .age(34)
                .build();

        existingUser.setAge(35);

        userService.update(existingUser);

        verify(userRepository).update(argThat(user ->
                user.getId() == 1L &&
                        user.getName().equals("Artem") &&
                        user.getEmail().equals("kwaka88@mail.ru") &&
                        user.getAge() == 35
        ));
    }
}