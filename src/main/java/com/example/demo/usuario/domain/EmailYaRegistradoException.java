package com.example.demo.usuario.domain;

/** El email ya pertenece a otro usuario. */
public class EmailYaRegistradoException extends RuntimeException {
    public EmailYaRegistradoException() {
        super("El email ya está registrado");
    }
}
