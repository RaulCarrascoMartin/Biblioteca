package com.example.demo.catalog.internal.domain.exception;

/** Ya existe otro libro con ese ISBN. */
public class DuplicateIsbnException extends RuntimeException {

    public DuplicateIsbnException() {
        super("El ISBN ya existe en el catálogo");
    }
}
