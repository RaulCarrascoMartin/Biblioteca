package com.example.demo.prestamo.internal.domain.exception;

/** El préstamo ya tiene fecha de devolución. */
public class PrestamoYaDevueltoException extends RuntimeException {
    public PrestamoYaDevueltoException() {
        super("El préstamo ya fue devuelto");
    }
}
