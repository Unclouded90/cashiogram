package com.mauro.cashiogram.exception;

/**
 * An exception that represents an internal server error.
 * This exception is thrown when an unexpected error occurs on the server-side.
 */
public class InternalServerException extends RuntimeException {
    /**
     * Constructs an InternalServerException with the specified error message.
     * This exception is thrown when an unexpected error occurs on the server-side.
     *
     * @param message the error message describing the internal server error
     */
    public InternalServerException(String message) {
        super(message);
    }
}
