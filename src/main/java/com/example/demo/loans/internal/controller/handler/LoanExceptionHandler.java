package com.example.demo.loans.internal.controller.handler;

import com.example.demo.loans.internal.domain.exception.InvalidLoanException;
import com.example.demo.loans.internal.domain.exception.LoanBookAlreadyAvailableException;
import com.example.demo.loans.internal.domain.exception.LoanConcurrentModificationException;
import com.example.demo.loans.internal.domain.exception.LoanBookNotAvailableException;
import com.example.demo.loans.internal.domain.exception.LoanBookNotFoundException;
import com.example.demo.loans.internal.controller.LoanController;
import com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.domain.exception.LoanUserNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

/**
 * Traduce a {@link ProblemDetail} (RFC 9457) las excepciones del módulo {@code loans} y las públicas de
 * {@code catalog} que se propagan al reservar y liberar libros. Además de {@code detail}, añade la propiedad
 * {@code error} con el mismo mensaje porque el frontend la lee (contrato sin cambios, D16).
 */
@RestControllerAdvice(basePackageClasses = LoanController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoanExceptionHandler {

    @ExceptionHandler({LoanNotFoundException.class, LoanUserNotFoundException.class, LoanBookNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({LoanAlreadyReturnedException.class, LoanBookNotAvailableException.class,
            LoanBookAlreadyAvailableException.class, LoanConcurrentModificationException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Invariantes del modelo que el formato del parámetro no cubre. */
    @ExceptionHandler(InvalidLoanException.class)
    public ProblemDetail handleInvalid(InvalidLoanException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    /** Parámetros que incumplen su restricción (p. ej. {@code libroId} no positivo). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleValidation(HandlerMethodValidationException ex) {
        List<String> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> parameterName(result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        problem.setTitle("Solicitud inválida");
        problem.setProperty("errors", errors);
        return problem;
    }

    private static String parameterName(String name, String message) {
        return (name == null ? "parameter" : name) + ": " + message;
    }

    private static ProblemDetail problem(HttpStatus status, String message) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setProperty("error", message);
        return problem;
    }
}
