package com.example.demo.auth.internal.domain;

import com.example.demo.auth.internal.domain.exception.InvalidCredentialsException;

public interface CredentialsAuthenticator {
    /** @throws InvalidCredentialsException si las credenciales no son válidas */
    void autenticar(String email, String password);
}
