package com.example.demo.users.dto;

/** Datos que el módulo de autenticación necesita para validar credenciales y emitir tokens. */
public record UserCredentials(String email, String password, String role, int tokenVersion) {

    @Override
    public String toString() {
        return "UserCredentials{email='" + email + "', role='" + role + "'}";
    }
}
