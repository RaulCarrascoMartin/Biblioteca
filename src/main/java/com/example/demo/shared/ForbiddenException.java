package com.example.demo.shared;

/** El solicitante no tiene permiso para realizar la acción. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {
        super("No tienes permiso para realizar esta acción");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
