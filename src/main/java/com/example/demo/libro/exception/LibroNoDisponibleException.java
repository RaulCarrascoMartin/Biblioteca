package com.example.demo.libro.exception;

/** El libro ya está prestado. */
public class LibroNoDisponibleException extends RuntimeException {
    public LibroNoDisponibleException() {
        super("El libro no se encuentra disponible en estos momentos");
    }
}
