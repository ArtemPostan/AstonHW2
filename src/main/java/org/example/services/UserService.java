package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.models.User;
import org.example.repository.UserRepository;
import org.example.util.HibernateUtil;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final Scanner scanner;

    public void start() {
        int choice = -1;
        while (choice != 0) {
            printMenu();
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
                processChoice(choice);
            } catch (InputMismatchException e) {
                System.out.println("Ошибка: Введите числовое значение.");
                scanner.nextLine(); // clear buffer
                choice = -1;
            } catch (Exception e) {
                System.out.println("Произошла ошибка: " + e.getMessage());
            }
        }
        System.out.println("👋 Завершение работы. Закрытие SessionFactory...");
        HibernateUtil.shutdown();
    }

    private void printMenu() {
        System.out.println("\n=== МЕНЮ ===");
        System.out.println("1. Создать нового пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя по ID");
        System.out.println("5. Удалить пользователя по ID");
        System.out.println("0. Выход");
        System.out.print("Выберите операцию: ");
    }

    private void processChoice(int choice) {
        switch (choice) {
            case 1:
                createUser();
                break;
            case 2:
                readUserById();
                break;
            case 3:
                readAllUsers();
                break;
            case 4:
                updateUser();
                break;
            case 5:
                deleteUser();
                break;
            case 0:
                break;
            default:
                System.out.println("Неизвестная команда. Попробуйте снова.");
        }
    }

    private void createUser() {
        System.out.print("Введите имя: ");
        String name = scanner.nextLine();
        System.out.print("Введите email: ");
        String email = scanner.nextLine();
        System.out.print("Введите возраст: ");
        int age = scanner.nextInt();
        scanner.nextLine();

        User user = User.builder().name(name).email(email).age(age).build();
        repository.save(user);
        System.out.println("Пользователь успешно создан. ID: " + user.getId());
    }

    private void readUserById() {
        System.out.print("Введите ID пользователя для поиска: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        User user = repository.findById(id);
        if (user != null) {
            System.out.println("Найден пользователь: " + user);
        } else {
            System.out.println("Пользователь с ID " + id + " не найден.");
        }
    }

    private void readAllUsers() {
        List<User> users = repository.findAll();
        if (users.isEmpty()) {
            System.out.println("В базе данных нет пользователей.");
        } else {
            System.out.println("--- Список всех пользователей (" + users.size() + ") ---");
            users.forEach(System.out::println);
            System.out.println("----------------------------------------");
        }
    }

    private void updateUser() {
        System.out.print("Введите ID пользователя для обновления: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        User existingUser = repository.findById(id);
        if (existingUser == null) {
            System.out.println("Пользователь с ID " + id + " не найден. Обновление невозможно.");
            return;
        }

        System.out.println("Текущее имя: " + existingUser.getName() + ". Введите новое имя (Enter, чтобы пропустить):");
        String newName = scanner.nextLine();
        if (!newName.isEmpty()) {
            existingUser.setName(newName);
        }

        System.out.println("Текущий возраст: " + existingUser.getAge() + ". Введите новый возраст (Enter, чтобы пропустить):");
        String newAgeStr = scanner.nextLine();
        if (!newAgeStr.isEmpty()) {
            try {
                existingUser.setAge(Integer.parseInt(newAgeStr));
            } catch (NumberFormatException e) {
                System.out.println("Возраст введен некорректно. Оставлено старое значение.");
            }
        }

        repository.update(existingUser);
        System.out.println("Пользователь успешно обновлен: " + existingUser);
    }

    private void deleteUser() {
        System.out.print("Введите ID пользователя для удаления: ");
        long id = scanner.nextLong();
        scanner.nextLine();

        User userToDelete = repository.findById(id);
        if (userToDelete != null) {
            repository.delete(id);
            System.out.println("Пользователь с ID " + id + " (" + userToDelete.getName() + ") успешно удален.");
        } else {
            System.out.println("Пользователь с ID " + id + " не найден. Удаление невозможно.");
        }
    }
}