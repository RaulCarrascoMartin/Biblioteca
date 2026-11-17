package com.example.demo.config;

import com.example.demo.shared.ForbiddenException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

/**
 * Errores transversales (validación, seguridad, genéricos). Siempre {@link ProblemDetail} (RFC 9457)
 * con {@code detail} y la propiedad {@code error} (el frontend lee {@code error}). Los errores de
 * negocio los traduce el handler de cada módulo, que tiene precedencia sobre este.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(ForbiddenException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /** Incluye AuthorizationDeniedException (subclase). */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "No tiene permisos para acceder a este recurso");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "No autenticado");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return validationProblem(errors);
    }

    /** Validación de parámetros (@Positive, @NotBlank... en argumentos de métodos del controller). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidation(HandlerMethodValidationException ex) {
        List<String> errors = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> result.getResolvableErrors().forEach(error ->
                errors.add(result.getMethodParameter().getParameterName() + ": " + error.getDefaultMessage())));
        return validationProblem(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return validationProblem(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedBody(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Cuerpo de la petición inválido");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParam(MissingServletRequestParameterException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Parámetro requerido faltante: " + ex.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Parámetro inválido: " + ex.getName());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "El dato ya existe en el sistema");
    }

    /** Fallback: 500 genérico sin detalles internos (se registran en el log). */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            // Errores propios de Spring MVC (405, 404, 415...): conservan su estado
            ProblemDetail body = errorResponse.getBody();
            body.setProperty("error", body.getDetail());
            return body;
        }
        log.error("Error no controlado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private static ProblemDetail validationProblem(List<String> errors) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, String.join("; ", errors));
        problem.setTitle("Solicitud inválida");
        problem.setProperty("errors", errors);
        return problem;
    }

    private static ProblemDetail problem(HttpStatus status, String message) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setProperty("error", message);
        return problem;
    }
}
