package com.example.demo.auth.domain;

import com.example.demo.auth.domain.InvalidCredentialsException;

public interface CredentialsAuthenticator {
    /** @throws InvalidCredentialsException si las credenciales no son válidas */
    void autenticar(String email, String password);
}
