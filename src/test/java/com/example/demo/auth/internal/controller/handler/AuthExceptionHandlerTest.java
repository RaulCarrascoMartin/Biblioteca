package com.example.demo.auth.internal.controller.handler;

import com.example.demo.shared.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuthExceptionHandlerTest {

    private final AuthExceptionHandler handler = new AuthExceptionHandler();

    @Test
    void invalidCredentialsIs401WithoutLeakingDetails() {
        ProblemDetail p = handler.handleInvalidCredentials(new InvalidCredentialsException());

        assertEquals(401, p.getStatus());
        assertEquals("Credenciales incorrectas", p.getDetail());
        assertEquals("Credenciales incorrectas", p.getProperties().get("error"));
    }

    @Test
    void validationIs400WithFieldErrors() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "registerDto");
        binding.addError(new FieldError("registerDto", "password", "La contraseña debe tener entre 8 y 72 caracteres"));
        MethodParameter parameter = new MethodParameter(
                AuthExceptionHandlerTest.class.getDeclaredMethod("dummy", String.class), 0);

        ProblemDetail p = handler.handleValidation(new MethodArgumentNotValidException(parameter, binding));

        assertEquals(400, p.getStatus());
        assertEquals(List.of("password: La contraseña debe tener entre 8 y 72 caracteres"),
                p.getProperties().get("errors"));
        assertFalse(p.getDetail().isBlank());
        assertEquals(p.getDetail(), p.getProperties().get("error"));
    }

    @SuppressWarnings("unused")
    private void dummy(String arg) {
    }
}
