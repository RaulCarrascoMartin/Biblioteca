package com.example.demo.catalog.internal.domain.exception;

/** El libro no existe. */
public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException() {
        super("Libro no encontrado");
    }
}
