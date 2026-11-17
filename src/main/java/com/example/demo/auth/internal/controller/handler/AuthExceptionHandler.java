package com.example.demo.auth.internal.controller.handler;

import com.example.demo.auth.internal.controller.AuthController;
import com.example.demo.shared.InvalidCredentialsException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce a {@link ProblemDetail} (RFC 9457) el error del módulo {@code auth}. La validación la traduce el
 * handler global. Añade la propiedad {@code error} porque el frontend la lee (contrato sin cambios).
 */
@RestControllerAdvice(basePackageClasses = AuthController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setProperty("error", ex.getMessage());
        return problem;
    }
}
