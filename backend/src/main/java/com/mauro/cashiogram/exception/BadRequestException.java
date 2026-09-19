package com.mauro.cashiogram.exception;

/**
 * Exception thrown when a request is invalid or malformed.
 * This exception is mapped to a 400 Bad Request HTTP status code.
 */
public class BadRequestException extends RuntimeException {
    /**
     * Constructs a new BadRequestException with the specified error message.
     *
     * @param message the error message describing the bad request
     */
    public BadRequestException(String message) {
        super(message);
    }
}
