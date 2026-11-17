package com.example.demo.loans.internal.domain.model;

import com.example.demo.loans.internal.domain.exception.InvalidLoanException;
import com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanTest {

    private static final LocalDate DAY = LocalDate.of(2026, 1, 2);

    @Test
    void constructor_acceptsAValidLoan() {
        Loan loan = new Loan(1L, 2L, 3L, DAY, DAY.plusDays(7));

        assertEquals(2L, loan.bookId());
        assertEquals(DAY.plusDays(7), loan.returnDate());
    }

    @Test
    void constructor_acceptsReturnOnTheSameDay() {
        assertEquals(DAY, new Loan(null, 2L, 3L, DAY, DAY).returnDate());
    }

    @ParameterizedTest
    @CsvSource(value = {"null,3,2026-01-02", "2,null,2026-01-02", "2,3,null"}, nullValues = "null")
    void constructor_rejectsNullMandatoryFields(Long bookId, Long userId, LocalDate loanDate) {
        assertThrows(InvalidLoanException.class, () -> new Loan(null, bookId, userId, loanDate, null));
    }

    @Test
    void constructor_rejectsReturnBeforeLoanDate() {
        assertThrows(InvalidLoanException.class, () -> new Loan(1L, 2L, 3L, DAY, DAY.minusDays(1)));
    }

    @Test
    void open_createsAnUnsavedUnreturnedLoan() {
        Loan loan = Loan.open(2L, 3L, DAY);

        assertNull(loan.id());
        assertNull(loan.returnDate());
        assertEquals(DAY, loan.loanDate());
        assertEquals(3L, loan.userId());
    }

    @Test
    void open_rejectsNullIds() {
        assertThrows(InvalidLoanException.class, () -> Loan.open(null, 3L, DAY));
        assertThrows(InvalidLoanException.class, () -> Loan.open(2L, null, DAY));
    }

    @Test
    void markReturned_returnsANewLoanWithTheDate() {
        Loan open = new Loan(1L, 2L, 3L, DAY, null);

        Loan returned = open.markReturned(DAY.plusDays(3));

        assertEquals(DAY.plusDays(3), returned.returnDate());
        assertEquals(1L, returned.id());
        assertNull(open.returnDate());
    }

    @Test
    void markReturned_twiceFails() {
        Loan returned = new Loan(1L, 2L, 3L, DAY, DAY);

        assertThrows(LoanAlreadyReturnedException.class, () -> returned.markReturned(DAY.plusDays(1)));
    }

    @Test
    void markReturned_beforeTheLoanDateFails() {
        Loan open = new Loan(1L, 2L, 3L, DAY, null);

        assertThrows(InvalidLoanException.class, () -> open.markReturned(DAY.minusDays(1)));
    }
}
