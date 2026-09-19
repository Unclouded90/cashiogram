package com.mauro.cashiogram.exception;

/**
 * Exception thrown when a conflict occurs, such as a resource already existing.
 */
public class ConflictException extends RuntimeException {
    /**
     * Constructs a new ConflictException with the specified error message.
     *
     * @param message the error message describing the conflict
     */
    public ConflictException(String message) {
        super(message);
    }
}
