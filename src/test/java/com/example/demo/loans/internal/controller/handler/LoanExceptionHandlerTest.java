package com.example.demo.loans.internal.controller.handler;

import com.example.demo.loans.internal.domain.exception.InvalidLoanException;
import com.example.demo.loans.internal.domain.exception.LoanBookAlreadyAvailableException;
import com.example.demo.loans.internal.domain.exception.LoanConcurrentModificationException;
import com.example.demo.loans.internal.domain.exception.LoanBookNotAvailableException;
import com.example.demo.loans.internal.domain.exception.LoanBookNotFoundException;
import com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.domain.exception.LoanUserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LoanExceptionHandlerTest {

    private final LoanExceptionHandler handler = new LoanExceptionHandler();

    @Test
    void notFoundExceptionsAre404WithErrorProperty() {
        for (RuntimeException ex : new RuntimeException[]{
                new LoanNotFoundException(), new LoanUserNotFoundException(), new LoanBookNotFoundException()}) {
            ProblemDetail p = handler.handleNotFound(ex);

            assertEquals(404, p.getStatus());
            assertFalse(p.getDetail().isBlank());
            assertEquals(p.getDetail(), p.getProperties().get("error"));
        }
    }

    @Test
    void conflictExceptionsAre409WithErrorProperty() {
        for (RuntimeException ex : new RuntimeException[]{
                new LoanAlreadyReturnedException(), new LoanBookNotAvailableException(),
                new LoanBookAlreadyAvailableException(), new LoanConcurrentModificationException(null)}) {
            ProblemDetail p = handler.handleConflict(ex);

            assertEquals(409, p.getStatus());
            assertEquals(p.getDetail(), p.getProperties().get("error"));
        }
    }

    @Test
    void invalidLoanIs422WithErrorProperty() {
        ProblemDetail p = handler.handleInvalid(new InvalidLoanException("El libro es obligatorio"));

        assertEquals(422, p.getStatus());
        assertEquals("El libro es obligatorio", p.getProperties().get("error"));
    }
}
