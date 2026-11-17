package com.example.demo.users.internal.domain.exception;

/** El usuario está en uso (tiene préstamos) y no puede eliminarse. */
public class UserWithLoansException extends RuntimeException {

    public UserWithLoansException() {
        super("No se puede eliminar un usuario con préstamos registrados");
    }
}
