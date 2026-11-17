package com.example.demo.catalog.internal.controller.handler;

import com.example.demo.catalog.internal.controller.BookController;
import com.example.demo.catalog.internal.domain.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotFoundException;
import com.example.demo.catalog.internal.domain.exception.BookWithLoansException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
import com.example.demo.catalog.internal.domain.exception.InvalidBookException;
import com.example.demo.catalog.internal.domain.exception.InvalidIsbnException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduce a HTTP (ProblemDetail, RFC 9457) las excepciones del módulo {@code catalog}. Solo actúa sobre
 * los controllers de este módulo.
 */
@RestControllerAdvice(basePackageClasses = BookController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BookExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        problem.setTitle("Solicitud inválida");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail handleNotFound(BookNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({DuplicateIsbnException.class, BookWithLoansException.class,
            BookNotAvailableException.class, BookAlreadyAvailableException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    /** Invariantes del modelo que el formato del DTO web no cubre (p. ej. género nulo al crear). */
    @ExceptionHandler({InvalidBookException.class, InvalidIsbnException.class})
    public ProblemDetail handleInvalid(RuntimeException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }
}
