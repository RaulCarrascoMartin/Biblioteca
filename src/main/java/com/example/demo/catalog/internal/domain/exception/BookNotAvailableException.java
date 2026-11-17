package com.example.demo.catalog.internal.domain.exception;

/** El libro ya está prestado. */
public class BookNotAvailableException extends RuntimeException {

    public BookNotAvailableException() {
        super("El libro no se encuentra disponible en estos momentos");
    }
}
