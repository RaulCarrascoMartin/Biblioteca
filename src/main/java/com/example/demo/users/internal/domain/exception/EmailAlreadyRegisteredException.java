package com.example.demo.users.internal.domain.exception;

/** El email ya pertenece a otro usuario. */
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException() {
        super("El email ya está registrado");
    }
}
