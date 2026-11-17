package com.example.demo.users.dto;

/** Datos que el módulo de autenticación necesita para validar credenciales y emitir tokens. */
public record UserCredentialsDto(String email, String password, String role, int tokenVersion) {

    @Override
    public String toString() {
        return "UserCredentialsDto{email='" + email + "', role='" + role + "'}";
    }
}
