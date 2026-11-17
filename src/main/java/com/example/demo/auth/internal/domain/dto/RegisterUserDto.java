package com.example.demo.auth.internal.domain.dto;

/** Datos de registro que recibe el servicio (la contraseña llega en claro). */
public record RegisterUserDto(String name, String email, String password) {

    @Override
    public String toString() {
        return "RegisterUserDto{name='" + name + "', email='" + email + "'}";
    }
}
