package com.example.demo.loans.internal.domain.exception;

/** El usuario del préstamo no existe. */
public class LoanUserNotFoundException extends RuntimeException {

    public LoanUserNotFoundException() {
        super("El usuario no existe");
    }
}
