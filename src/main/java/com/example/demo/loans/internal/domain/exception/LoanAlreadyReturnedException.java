package com.example.demo.loans.internal.domain.exception;

/** El préstamo ya tiene fecha de devolución. */
public class LoanAlreadyReturnedException extends RuntimeException {

    public LoanAlreadyReturnedException() {
        super("El préstamo ya fue devuelto");
    }
}
