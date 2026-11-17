package com.example.demo.users.internal.domain.dto;

/** Contrato del servicio para registrar un usuario; la contraseña llega en claro. */
public record NewUserDto(String name, String email, String password) {

    @Override
    public String toString() {
        return "NewUserDto{name='" + name + "', email='" + email + "'}";
    }
}
