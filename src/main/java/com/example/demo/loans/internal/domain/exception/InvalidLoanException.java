package com.example.demo.loans.internal.domain.exception;

/** El préstamo incumple una invariante del modelo. */
public class InvalidLoanException extends RuntimeException {

    public InvalidLoanException(String message) {
        super(message);
    }
}
