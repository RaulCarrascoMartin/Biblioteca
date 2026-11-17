package com.example.demo.users.internal.domain.exception;

/** El email no tiene un formato válido. */
public class InvalidEmailException extends RuntimeException {

    public InvalidEmailException(String value) {
        super("El email no tiene un formato válido: " + value);
    }
}
