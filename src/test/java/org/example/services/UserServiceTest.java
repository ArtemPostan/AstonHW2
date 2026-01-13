package org.example.services;

import org.example.dto.UserDTO;
import org.example.models.User;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Должен успешно создать пользователя и вернуть DTO")
    void shouldCreateUserSuccessfully() {
        // Given
        UserDTO inputDto = UserDTO.builder()
                .name("Ivan")
                .email("ivan@mail.com")
                .age(25)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@mail.com")
                .age(25)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDTO result = userService.createUser(inputDto);

        // Then
        assertNotNull(result.getId());
        assertEquals("Ivan", result.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Должен выбросить RuntimeException, если пользователь не найден")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.findById(id)
        );

        assertEquals("User not found with id: " + id, exception.getMessage());
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Должен обновить данные пользователя")
    void shouldUpdateUser() {
        // Given
        long id = 1L;
        UserDTO updateInfo = UserDTO.builder()
                .name("New Name")
                .email("new@mail.com")
                .age(30)
                .build();

        User existingUser = User.builder()
                .id(id)
                .name("Old Name")
                .email("old@mail.com")
                .age(20)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));

        // When
        userService.update(id, updateInfo);

        // Then
        verify(userRepository).save(argThat(user ->
                user.getName().equals("New Name") && user.getEmail().equals("new@mail.com")
        ));
    }

    @Test
    @DisplayName("Должен вернуть true, если удаление прошло успешно")
    void shouldReturnTrueWhenDeleteSuccessful() {
        // Given
        long id = 1L;
        when(userRepository.existsById(id)).thenReturn(true);

        // When
        boolean result = userService.delete(id);

        // Then
        assertTrue(result);
        verify(userRepository).deleteById(id);
    }
}