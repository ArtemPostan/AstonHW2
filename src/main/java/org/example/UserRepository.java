package org.example;

import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.postgresql.util.PSQLException;

public class UserRepository {

    public void save(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
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
            if (e.getCause() instanceof PSQLException) {
                PSQLException psqlException = (PSQLException) e.getCause();
                if ("23505".equals(psqlException.getSQLState())) {
                    throw new DataIntegrityViolationException("Пользователь с таким Email уже существует.", e);
                }
            }
            throw new DataIntegrityViolationException("Нарушение ограничения целостности данных.", e);
        }
        catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving user: " + user.getName(), e);
        } finally {
            session.close();
        }
    }

    public User findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        User user = null;
        try {
            user = session.get(User.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user with ID: " + id, e);
        } finally {
            session.close();
        }
        return user;
    }

    public List<User> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("from User", User.class).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving all users", e);
        } finally {
            session.close();
        }
    }

    public User update(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;
        User updatedUser = null;
        try {
            transaction = session.beginTransaction();
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