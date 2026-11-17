package com.example.demo.loans.internal.domain.exception;

/** El préstamo no existe. */
public class LoanNotFoundException extends RuntimeException {

    public LoanNotFoundException() {
        super("Préstamo no encontrado");
    }
}
