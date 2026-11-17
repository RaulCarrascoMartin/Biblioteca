package com.example.demo.catalog.exception;

/** Se intenta devolver un libro que ya está disponible. */
public class BookAlreadyAvailableException extends RuntimeException {

    public BookAlreadyAvailableException() {
        super("El libro ya se encuentra disponible");
    }
}
