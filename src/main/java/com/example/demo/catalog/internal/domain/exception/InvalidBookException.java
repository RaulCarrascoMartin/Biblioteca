package com.example.demo.catalog.internal.domain.exception;

/** Los datos del libro incumplen una invariante del modelo. */
public class InvalidBookException extends RuntimeException {

    public InvalidBookException(String message) {
        super(message);
    }
}
