package com.example.demo.usuario.internal.domain.exception;

/** El usuario tiene préstamos y no puede eliminarse. */
public class UsuarioConPrestamosException extends RuntimeException {
    public UsuarioConPrestamosException() {
        super("No se puede eliminar un usuario con préstamos registrados");
    }
}
