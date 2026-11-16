package com.example.demo.auth.domain;

public interface CredentialsAuthenticator {
    /** @throws InvalidCredentialsException si las credenciales no son válidas */
    void autenticar(String email, String password);
}
