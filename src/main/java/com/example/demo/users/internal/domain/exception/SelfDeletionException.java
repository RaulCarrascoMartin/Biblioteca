package com.example.demo.users.internal.domain.exception;

/** Un usuario intenta eliminar su propia cuenta. */
public class SelfDeletionException extends RuntimeException {

    public SelfDeletionException() {
        super("No puede eliminar su propia cuenta");
    }
}
