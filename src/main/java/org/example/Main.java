package org.example;

import org.example.utils.HibernateUtil;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        UserRepository repository = new UserRepository();

        // --- C: CREATE (Сохранение) ---
        User user1 = new User("Anna", "anna@example.com", 28);
        User user2 = new User("Bob", "bob@example.com", 35);

        System.out.println("--- 1. Создание ---");
        repository.save(user1);
        repository.save(user2);
        System.out.println("Создано: " + user1.getName() + " (ID: " + user1.getId() + ")");

        // --- R: READ (Чтение всех) ---
        System.out.println("\n--- 2. Чтение всех ---");
        List<User> allUsers = repository.findAll();
        allUsers.forEach(System.out::println);

        // --- U: UPDATE (Обновление) ---
        System.out.println("\n--- 3. Обновление ---");
        User anna = repository.findById(user1.getId());
        if (anna != null) {
            anna.setName("Anna Petrovna"); // Изменение данных
            anna.setAge(29);
            repository.update(anna);
            System.out.println("Обновлено: " + anna);
        }

        // --- D: DELETE (Удаление) ---
        System.out.println("\n--- 4. Удаление ---");
        Long idToDelete = user2.getId();
        repository.delete(idToDelete);
        System.out.println("Пользователь с ID " + idToDelete + " удален.");

        // Проверка удаления
        User deletedUser = repository.findById(idToDelete);
        System.out.println("Проверка (ожидается null): " + deletedUser);

        // Закрытие SessionFactory при завершении
        HibernateUtil.shutdown();
    }
}