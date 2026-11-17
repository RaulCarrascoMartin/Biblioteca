package com.example.demo.users.internal.domain.exception;

/** Los datos de un usuario violan una invariante del modelo. */
public class InvalidUserException extends RuntimeException {

    public InvalidUserException(String message) {
        super(message);
    }
}
