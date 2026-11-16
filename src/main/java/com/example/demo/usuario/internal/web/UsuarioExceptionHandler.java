package com.example.demo.usuario.internal.web;

import com.example.demo.usuario.internal.domain.exception.AutoEliminacionException;
import com.example.demo.usuario.internal.domain.exception.EmailYaRegistradoException;
import com.example.demo.usuario.internal.domain.exception.UsuarioConPrestamosException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** Traduce a HTTP las excepciones de negocio del módulo {@code usuario}. */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UsuarioExceptionHandler {

    @ExceptionHandler({UsuarioConPrestamosException.class, AutoEliminacionException.class})
    public ResponseEntity<Map<String, String>> handleConflicto(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(EmailYaRegistradoException.class)
    public ResponseEntity<Map<String, String>> handleEmailYaRegistrado(EmailYaRegistradoException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
