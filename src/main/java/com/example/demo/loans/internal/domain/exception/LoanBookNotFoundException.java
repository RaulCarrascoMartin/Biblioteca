package com.example.demo.loans.internal.domain.exception;

/** El libro del préstamo no existe en el catálogo. */
public class LoanBookNotFoundException extends RuntimeException {

    public LoanBookNotFoundException() {
        super("Libro no encontrado");
    }
}
