package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.models.User;
import org.example.repository.UserRepository;

import java.util.List;


@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User createUser(String name, String email, int age) {
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