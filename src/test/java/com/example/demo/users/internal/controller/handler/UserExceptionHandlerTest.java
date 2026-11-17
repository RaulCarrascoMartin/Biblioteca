package com.example.demo.users.internal.controller.handler;

import com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException;
import com.example.demo.users.internal.domain.exception.SelfDeletionException;
import com.example.demo.users.internal.domain.exception.UserNotFoundException;
import com.example.demo.users.internal.domain.exception.UserWithLoansException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserExceptionHandlerTest {

    private final UserExceptionHandler handler = new UserExceptionHandler();

    @Test
    void notFoundIs404() {
        ProblemDetail p = handler.handleNotFound(new UserNotFoundException(5L));

        assertEquals(404, p.getStatus());
        assertEquals(p.getDetail(), p.getProperties().get("error"));
    }

    @Test
    void conflictsAre409() {
        assertEquals(409, handler.handleConflict(new UserWithLoansException()).getStatus());
        assertEquals(409, handler.handleConflict(new SelfDeletionException()).getStatus());
    }

    @Test
    void emailAlreadyRegisteredKeepsHistoricalStatus400() {
        ProblemDetail p = handler.handleEmailAlreadyRegistered(new EmailAlreadyRegisteredException());

        assertEquals(400, p.getStatus());
        assertFalse(p.getDetail().isBlank());
    }
}
