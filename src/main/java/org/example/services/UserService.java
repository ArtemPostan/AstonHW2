package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserDTO;
import org.example.exception.DataIntegrityViolationException;
import org.example.models.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

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
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
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