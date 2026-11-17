package com.example.demo.catalog.internal.domain.exception;

/** El ISBN no cumple el formato exigido. */
public class InvalidIsbnException extends RuntimeException {

    public InvalidIsbnException(String value) {
        super("ISBN inválido: debe tener entre 10 y 20 caracteres (recibido: %s)".formatted(value));
    }
}
