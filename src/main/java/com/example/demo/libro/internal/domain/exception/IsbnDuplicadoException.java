package com.example.demo.libro.internal.domain.exception;

/** Ya existe otro libro con ese ISBN. */
public class IsbnDuplicadoException extends RuntimeException {
    public IsbnDuplicadoException() {
        super("El ISBN ya existe en el catálogo");
    }
}
