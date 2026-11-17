package com.example.demo.loans.internal.domain.model;

import com.example.demo.loans.internal.domain.exception.InvalidLoanException;
import com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException;

import java.time.LocalDate;

/**
 * Préstamo. Inmutable: sus operaciones devuelven un préstamo nuevo. Referencia al libro y al usuario solo
 * por id (pertenecen a otros módulos).
 */
public record Loan(Long id, Long bookId, Long userId, LocalDate loanDate, LocalDate returnDate) {

    public Loan {
        if (bookId == null) {
            throw new InvalidLoanException("El libro es obligatorio");
        }
        if (userId == null) {
            throw new InvalidLoanException("El usuario es obligatorio");
        }
        if (loanDate == null) {
            throw new InvalidLoanException("La fecha de préstamo es obligatoria");
        }
        if (returnDate != null && returnDate.isBefore(loanDate)) {
            throw new InvalidLoanException("La fecha de devolución no puede ser anterior a la del préstamo");
        }
    }

    /** Préstamo nuevo (sin id) y sin devolver. */
    public static Loan open(Long bookId, Long userId, LocalDate today) {
        return new Loan(null, bookId, userId, today, null);
    }

    /** Registra la devolución. */
    public Loan markReturned(LocalDate today) {
        if (returnDate != null) {
            throw new LoanAlreadyReturnedException();
        }
        return new Loan(id, bookId, userId, loanDate, today);
    }
}
