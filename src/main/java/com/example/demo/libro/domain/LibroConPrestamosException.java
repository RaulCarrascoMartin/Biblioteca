package com.example.demo.libro.domain;

/** El libro tiene préstamos y no puede retirarse. */
public class LibroConPrestamosException extends RuntimeException {
    public LibroConPrestamosException() {
        super("No se puede retirar un libro con préstamos registrados");
    }
}
