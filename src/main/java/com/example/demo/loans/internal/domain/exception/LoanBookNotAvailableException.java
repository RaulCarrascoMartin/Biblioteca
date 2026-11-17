package com.example.demo.loans.internal.domain.exception;

/** El libro ya está prestado. */
public class LoanBookNotAvailableException extends RuntimeException {

    public LoanBookNotAvailableException() {
        super("El libro no se encuentra disponible en estos momentos");
    }
}
