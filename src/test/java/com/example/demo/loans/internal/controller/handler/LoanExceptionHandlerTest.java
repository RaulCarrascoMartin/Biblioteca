package com.example.demo.loans.internal.controller.handler;

import com.example.demo.catalog.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.exception.BookNotAvailableException;
import com.example.demo.catalog.exception.BookNotFoundException;
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
                new LoanNotFoundException(), new LoanUserNotFoundException(), new BookNotFoundException()}) {
            ProblemDetail p = handler.handleNotFound(ex);

            assertEquals(404, p.getStatus());
            assertFalse(p.getDetail().isBlank());
            assertEquals(p.getDetail(), p.getProperties().get("error"));
        }
    }

    @Test
    void conflictExceptionsAre409WithErrorProperty() {
        for (RuntimeException ex : new RuntimeException[]{
                new LoanAlreadyReturnedException(), new BookNotAvailableException(),
                new BookAlreadyAvailableException()}) {
            ProblemDetail p = handler.handleConflict(ex);

            assertEquals(409, p.getStatus());
            assertEquals(p.getDetail(), p.getProperties().get("error"));
        }
    }
}
