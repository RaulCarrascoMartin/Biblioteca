package com.example.demo.domain.port;

import com.example.demo.domain.exception.InvalidCredentialsException;

public interface CredentialsAuthenticator {
    /** @throws InvalidCredentialsException si las credenciales no son válidas */
    void autenticar(String email, String password);
}
