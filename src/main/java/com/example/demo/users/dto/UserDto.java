package com.example.demo.users.dto;

/** Datos para registrar un usuario (la contraseña llega en claro y se hashea en el servicio). */
public record UserDto(String name, String email, String password) {

    @Override
    public String toString() {
        return "UserDto{name='" + name + "', email='" + email + "'}";
    }
}
