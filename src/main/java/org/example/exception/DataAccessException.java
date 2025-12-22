package org.example.exception;

/**
 * Общее исключение, указывающее на ошибку, произошедшую на уровне доступа к данным.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}