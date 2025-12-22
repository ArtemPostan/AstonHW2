package org.example.exception;

/**
 * Исключение, указывающее на нарушение ограничений целостности данных
 */
public class DataIntegrityViolationException extends DataAccessException {

    public DataIntegrityViolationException(String message) {
        super(message);
    }

    public DataIntegrityViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}