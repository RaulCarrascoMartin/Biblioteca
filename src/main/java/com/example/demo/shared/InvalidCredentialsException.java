package com.example.demo.shared;

/** Las credenciales (email y contraseña) no son válidas. No distingue email inexistente de contraseña errónea. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Credenciales incorrectas");
    }
}
