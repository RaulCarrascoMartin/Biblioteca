package com.example.demo.usuario.dto;

/** Datos que el módulo de autenticación necesita para validar credenciales y emitir tokens. */
public record UsuarioCredencialesDTO(String email, String password, String rol, int tokenVersion) {

    @Override
    public String toString() {
        return "UsuarioCredencialesDTO{email='" + email + "', rol='" + rol + "'}";
    }
}
