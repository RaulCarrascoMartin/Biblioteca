package com.example.demo.catalog.internal.controller.handler;

import com.example.demo.catalog.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.exception.BookNotAvailableException;
import com.example.demo.catalog.exception.BookNotFoundException;
import com.example.demo.catalog.internal.controller.BookController;
import com.example.demo.catalog.internal.domain.exception.BookWithLoansException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
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
 * los controllers de este módulo; quien llame a {@code CatalogModule} desde otro controller debe traducir
 * las excepciones públicas ({@code catalog.exception}) en su propio handler.
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
}
