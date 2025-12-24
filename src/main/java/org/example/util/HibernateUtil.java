package org.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration().configure();
            String testUrl = System.getProperty("hibernate.connection.url");
            if (testUrl != null) {
                configuration.setProperty("hibernate.connection.url", testUrl);
                configuration.setProperty("hibernate.connection.username", System.getProperty("hibernate.connection.username"));
                configuration.setProperty("hibernate.connection.password", System.getProperty("hibernate.connection.password"));
                configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            }

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Initial SessionFactory creation failed." + ex);
        }
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}