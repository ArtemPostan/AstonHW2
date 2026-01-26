package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserAction;
import org.example.dto.UserDTO;
import org.example.dto.UserEvent;
import org.example.exception.DataIntegrityViolationException;
import org.example.models.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (repository.existsByEmail(userDTO.getEmail())) {
            throw new DataIntegrityViolationException(
                    "Пользователь с email " + userDTO.getEmail() + " уже существует"
            );
        }

        User user = User.builder()
                .name(userDTO.getName())
                .email(userDTO.getEmail())
                .age(userDTO.getAge())
                .build();

        User savedUser = repository.save(user);

        // 3. Создаем событие для Kafka
        UserEvent event = UserEvent.builder()
                .email(savedUser.getEmail())
                .action(UserAction.CREATE)
                .build();

        // 4. Отправляем в Kafka
        try {
            System.out.println("LOG: Отправка в Kafka топик '" + topic + "' для: " + savedUser.getEmail());
            kafkaTemplate.send(topic, event);
            System.out.println("LOG: Успешно отправлено!");
        } catch (Exception e) {
            System.err.println("LOG ERROR: Ошибка Kafka: " + e.getMessage());
        }
        return convertToDTO(savedUser);
    }

    public UserDTO findById(long id) {
        return repository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public List<UserDTO> findAll() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, UserDTO userDTO) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());

        repository.save(user);
    }

    @Transactional
    public boolean delete(long id) {
        // 1. Сначала ищем пользователя, чтобы достать его email
        return repository.findById(id).map(user -> {
            String email = user.getEmail();

            // 2. Удаляем из базы
            repository.delete(user);

            // 3. Отправляем событие удаления в Kafka
            UserEvent event = UserEvent.builder()
                    .email(email)
                    .action(UserAction.DELETE)
                    .build();

            try {
                kafkaTemplate.send(topic, event);
                System.out.println("LOG: Отправлено событие удаления для: " + email);
            } catch (Exception e) {
                System.err.println("LOG ERROR: Не удалось отправить событие удаления: " + e.getMessage());
            }

            return true;
        }).orElse(false); // Если id не найден, вернем false
    }


    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .build();
    }


}