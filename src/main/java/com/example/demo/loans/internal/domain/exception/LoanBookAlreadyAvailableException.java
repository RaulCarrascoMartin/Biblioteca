package com.example.demo.loans.internal.domain.exception;

/** El libro ya estaba disponible al devolverlo. */
public class LoanBookAlreadyAvailableException extends RuntimeException {

    public LoanBookAlreadyAvailableException() {
        super("El libro ya se encuentra disponible");
    }
}
