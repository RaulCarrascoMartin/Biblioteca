package com.example.demo.loans.internal.domain.exception;

/** El préstamo lo modificó otra transacción a la vez (bloqueo optimista); se puede reintentar. */
public class LoanConcurrentModificationException extends RuntimeException {

    public LoanConcurrentModificationException(Throwable cause) {
        super("El préstamo fue modificado por otra operación; inténtalo de nuevo", cause);
    }
}
