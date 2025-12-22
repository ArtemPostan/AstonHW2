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
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                session.persist(user);
                transaction.commit();

            } catch (ConstraintViolationException e) {
                // 1. Обработка нарушения ограничений
                if (transaction != null) {
                    transaction.rollback();
                }
                log.warn("Ошибка целостности при сохранении: {}", e.getMessage());
                if (e.getCause() instanceof PSQLException) {
                    PSQLException psqlException = (PSQLException) e.getCause();
                    if ("23505".equals(psqlException.getSQLState())) {
                        throw new DataIntegrityViolationException("Пользователь с таким Email уже существует.", e);
                    }
                }
                throw new DataIntegrityViolationException("Нарушение ограничения целостности данных.", e);
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                log.error("Критическая ошибка сохранения пользователя: {}", user.getName(), e);
                throw new RuntimeException(e);
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
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw new RuntimeException("Error updating user with ID: " + user.getId(), e);
            }

        }
    }

    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
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
        } finally {
            session.close();
        }
    }
}