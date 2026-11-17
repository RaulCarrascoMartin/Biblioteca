package com.example.demo.catalog.internal.domain.exception;

/** El libro está en uso (p. ej. tiene préstamos) y no puede retirarse. */
public class BookWithLoansException extends RuntimeException {

    public BookWithLoansException() {
        super("No se puede retirar un libro con préstamos registrados");
    }
}
