package com.example.demo.shared;

/**
 * Puerto técnico: comprueba unas credenciales. La implementación (Spring Security) vive en
 * {@code config}, por eso el contrato está en {@code shared} y no en un módulo.
 */
public interface CredentialsAuthenticator {

    /** @throws InvalidCredentialsException si las credenciales no son válidas */
    void authenticate(String email, String password);
}
