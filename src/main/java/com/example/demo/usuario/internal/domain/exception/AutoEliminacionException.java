package com.example.demo.usuario.internal.domain.exception;

/** Un usuario intenta eliminar su propia cuenta. */
public class AutoEliminacionException extends RuntimeException {
    public AutoEliminacionException() {
        super("No puede eliminar su propia cuenta");
    }
}
