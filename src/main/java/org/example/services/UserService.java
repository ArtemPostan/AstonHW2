package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.exception.DataIntegrityViolationException;
import org.example.models.User;
import org.example.repository.UserRepository;

import java.util.List;


@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User createUser(String name, String email, int age) {
        if (name == null || name.isBlank()) {
            throw new DataIntegrityViolationException("Имя пользователя не может быть пустым");
        }

        if (email == null || email.isBlank()) {
            throw new DataIntegrityViolationException("Email обязателен для заполнения");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new DataIntegrityViolationException("Некорректный формат Email: " + email);
        }

        if (age < 0 || age > 150) {
            throw new DataIntegrityViolationException("Указан недопустимый возраст: " + age);
        }
        User user = User.builder().name(name).email(email).age(age).build();
        repository.save(user);
        return user;
    }

    public User findById(long id) {
        return repository.findById(id);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public void update(User user) {
        repository.update(user);
    }

    public boolean delete(long id) {
        User user = repository.findById(id);
        if (user != null) {
            repository.delete(id);
            return true;
        }
        return false;
    }
}