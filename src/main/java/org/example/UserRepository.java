package org.example;

import org.example.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class UserRepository {

    // --- 1. CREATE (Создание) ---
    public void save(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving user: " + user.getName(), e);
        } finally {
            session.close();
        }
    }

    // --- 2. READ (Чтение по ID) ---
    public User findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = null;
        try {
            // Для чтения транзакция не нужна, но используем try-finally для закрытия сессии
            user = session.get(User.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user with ID: " + id, e);
        } finally {
            session.close();
        }
        return user;
    }

    // --- 2. READ (Чтение всех) ---
    public List<User> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            // Используем HQL для запроса всех объектов User
            return session.createQuery("from User", User.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving all users", e);
        } finally {
            session.close();
        }
    }

    // --- 3. UPDATE (Обновление) ---
    public User update(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        User updatedUser = null;
        try {
            transaction = session.beginTransaction();
            // Используем merge для обновления Detached-объекта
            updatedUser = session.merge(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating user with ID: " + user.getId(), e);
        } finally {
            session.close();
        }
        return updatedUser;
    }

    // --- 4. DELETE (Удаление) ---
    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            // Сначала получаем объект, чтобы убедиться, что он присоединен или существует
            User userToDelete = session.get(User.class, id);

            if (userToDelete != null) {
                session.remove(userToDelete);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting user with ID: " + id, e);
        } finally {
            session.close();
        }
    }
}