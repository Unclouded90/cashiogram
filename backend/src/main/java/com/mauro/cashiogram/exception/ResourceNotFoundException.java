package com.mauro.cashiogram.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructs a new ResourceNotFoundException with the specified error message
     * 
     * @param message the error message describing that the resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
