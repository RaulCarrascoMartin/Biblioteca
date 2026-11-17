package com.example.demo.users.internal.domain.dto;

/** Contrato del servicio para autenticación: incluye el hash de la contraseña. */
public record UserCredentialsDto(String email, String passwordHash, String role, int tokenVersion) {

    @Override
    public String toString() {
        return "UserCredentialsDto{email='" + email + "', role='" + role + "'}";
    }
}
