package com.example.demo.prestamo.internal.web;

import com.example.demo.prestamo.internal.domain.exception.PrestamoYaDevueltoException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Traduce a HTTP las excepciones de negocio del módulo {@code prestamo}. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrestamoExceptionHandler {

    @ExceptionHandler(PrestamoYaDevueltoException.class)
    public ResponseEntity<Map<String, String>> handleYaDevuelto(PrestamoYaDevueltoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
