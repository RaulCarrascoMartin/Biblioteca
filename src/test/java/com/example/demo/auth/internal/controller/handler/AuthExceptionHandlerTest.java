package com.example.demo.auth.internal.controller.handler;

import com.example.demo.shared.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ProblemDetail;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthExceptionHandlerTest {

    private final AuthExceptionHandler handler = new AuthExceptionHandler();

    @Test
    void invalidCredentialsIs401WithoutLeakingDetails() {
        ProblemDetail p = handler.handleInvalidCredentials(new InvalidCredentialsException());

        assertEquals(401, p.getStatus());
        assertEquals("Credenciales incorrectas", p.getDetail());
        assertEquals("Credenciales incorrectas", p.getProperties().get("error"));
    }
}
