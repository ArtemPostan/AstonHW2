package org.example.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.exception.DataIntegrityViolationException;
import org.example.util.HibernateUtil;
import org.example.models.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;

@Slf4j
public class UserRepository {

    public void save(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                session.persist(user);
                transaction.commit();
            } catch (org.hibernate.exception.ConstraintViolationException e) {
                if (transaction != null) transaction.rollback();

                // Обработка ошибок БД (Unique, Foreign Key и т.д.)
                String sqlState = "";
                if (e.getCause() instanceof org.postgresql.util.PSQLException psqlEx) {
                    sqlState = psqlEx.getSQLState();
                }

                if ("23505".equals(sqlState)) {
                    throw new DataIntegrityViolationException("Пользователь с email " + user.getEmail() + " уже существует.", e);
                }
                throw new DataIntegrityViolationException("Нарушение ограничений базы данных.", e);

            } catch (org.hibernate.PropertyValueException e) {
                // Ловит нарушение @Column(nullable = false) до отправки в БД
                if (transaction != null) transaction.rollback();
                log.warn("Попытка сохранить null в обязательное поле: {}", e.getPropertyName());
                throw new DataIntegrityViolationException("Поле '" + e.getPropertyName() + "' обязательно для заполнения.", e);

            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                log.error("Непредвиденная ошибка при сохранении пользователя: {}", user.getName(), e);
                throw new RuntimeException("Ошибка сохранения данных", e);
            }
        }
    }

    public User findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            return session.get(User.class, id);
        } catch (Exception e) {
            log.error("Ошибка поиска пользователя с ID: {}", id, e);
            throw new RuntimeException(e);
            }
    }

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            try {
                return session.createQuery("from User", User.class).getResultList();
            } catch (Exception e) {
                log.error("Ошибка получения всех пользователей", e);
                throw new RuntimeException(e);
            }
        }
    }

    public User update(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                session.createMutationQuery(
                                "update User set name = :name, email = :email, age = :age where id = :id")
                        .setParameter("name", user.getName())
                        .setParameter("email", user.getEmail())
                        .setParameter("age", user.getAge())
                        .setParameter("id", user.getId())
                        .executeUpdate();
                transaction.commit();
                return user;
            } catch (org.hibernate.exception.ConstraintViolationException e) {
                if (transaction != null) transaction.rollback();
                // Обработка SQL ошибок (например, NOT NULL или UNIQUE)
                throw new DataIntegrityViolationException("Ошибка обновления: нарушение ограничений БД (возможно, email уже занят или null)", e);
            } catch (org.hibernate.PropertyValueException e) {
                if (transaction != null) transaction.rollback();
                // Ошибка, если в поле @Column(nullable = false) попал null
                throw new DataIntegrityViolationException("Поле '" + e.getPropertyName() + "' обязательно для заполнения", e);
            } catch (Exception e) {
                if (transaction != null) transaction.rollback();
                log.error("Непредвиденная ошибка при обновлении пользователя ID: {}", user.getId(), e);
                throw new RuntimeException("Error updating user with ID: " + user.getId(), e);
            }
        }

    }

    public void delete(Long id) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
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
            }
        }
    }
}