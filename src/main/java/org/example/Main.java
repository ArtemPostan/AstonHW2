package org.example;

import org.example.models.User;
import org.example.repository.UserRepository;
import org.example.services.UserService;
import org.example.util.HibernateUtil;
import org.example.exception.DataIntegrityViolationException;

import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final UserService userService = new UserService(new UserRepository());

    public static void main(String[] args) {
        int choice = -1;
        while (choice != 0) {
            printMenu();
            try {
                choice = Integer.parseInt(scanner.nextLine());
                processChoice(choice);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите число.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        HibernateUtil.shutdown();
    }

    private static void printMenu() {
        System.out.println("\n1. Создать | 2. Найти | 3. Все | 4. Обновить | 5. Удалить | 0. Выход");
        System.out.print("Выберите операцию: ");
    }

    private static void processChoice(int choice) {
        switch (choice) {
            case 1 -> handleCreate();
            case 2 -> handleRead();
            case 3 -> handleReadAll();
            case 4 -> handleUpdate();
            case 5 -> handleDelete();
            case 0 -> System.out.println("Выход...");
            default -> System.out.println("Неверный ввод.");
        }
    }

    private static void handleCreate() {
        System.out.print("Имя: ");
        String name = scanner.nextLine();
        // Если введена пустая строка, передаем null
        String nameToSave = name.isBlank() ? null : name;

        System.out.print("Email: ");
        String email = scanner.nextLine();
        // Если нажат Enter, передаем null, чтобы сработал nullable = false
        String emailToSave = email.isBlank() ? null : email;

        System.out.print("Возраст: ");
        String ageInput = scanner.nextLine();
        Integer age = ageInput.isBlank() ? null : Integer.parseInt(ageInput);

        try {
            User user = userService.createUser(nameToSave, emailToSave, age);
            System.out.println("Создан с ID: " + user.getId());
        } catch (DataIntegrityViolationException e) {
            // Теперь вы поймаете ошибку "Поле 'email' обязательно для заполнения"
            System.out.println("Ошибка валидации: " + e.getMessage());
        }
    }

    private static void handleRead() {
        System.out.print("ID: ");
        long id = Long.parseLong(scanner.nextLine());
        User user = userService.findById(id);
        System.out.println(user != null ? user : "Не найден.");
    }

    private static void handleReadAll() {
        userService.findAll().forEach(System.out::println);
    }

    private static void handleUpdate() {
        System.out.print("ID для обновления: ");
        long id = Long.parseLong(scanner.nextLine());
        User user = userService.findById(id);

        if (user == null) {
            System.out.println("Не найден.");
            return;
        }

        System.out.print("Новое имя (Enter чтобы оставить " + user.getName() + "): ");
        String name = scanner.nextLine();
        if (!name.isEmpty()) {
            user.setName(name);
        }

        System.out.print("Новый email (Текущий: " + user.getEmail() + "). Enter, чтобы пропустить: ");
        String email = scanner.nextLine();
        if (!email.isEmpty()) {
            user.setEmail(email);
        }

        System.out.print("Новый возраст (Текущий: " + user.getAge() + "). Enter, чтобы пропустить: ");
        String ageStr = scanner.nextLine();
        if (!ageStr.isEmpty()) {
            try {
                user.setAge(Integer.parseInt(ageStr));
            } catch (NumberFormatException e) {
                System.out.println("Ошибка формата. Оставлено старое значение.");
            }
        }

        userService.update(user);
        System.out.println("Обновлено.");
    }

    private static void handleDelete() {
        System.out.print("ID для удаления: ");
        long id = Long.parseLong(scanner.nextLine());
        if (userService.delete(id)) {
            System.out.println("Удалено.");
        } else {
            System.out.println("Не найден.");
        }

    }
}
