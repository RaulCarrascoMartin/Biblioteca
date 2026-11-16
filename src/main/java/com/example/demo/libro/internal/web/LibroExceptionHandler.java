package com.example.demo.libro.internal.web;

import com.example.demo.libro.exception.LibroNoDisponibleException;
import com.example.demo.libro.internal.domain.exception.IsbnDuplicadoException;
import com.example.demo.libro.internal.domain.exception.LibroConPrestamosException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Traduce a HTTP las excepciones de negocio del módulo {@code libro}. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LibroExceptionHandler {

    /** El cuerpo es texto plano, como antes de extraer la excepción. */
    @ExceptionHandler(IsbnDuplicadoException.class)
    public ResponseEntity<String> handleIsbnDuplicado(IsbnDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler({LibroConPrestamosException.class, LibroNoDisponibleException.class})
    public ResponseEntity<Map<String, String>> handleConflicto(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
