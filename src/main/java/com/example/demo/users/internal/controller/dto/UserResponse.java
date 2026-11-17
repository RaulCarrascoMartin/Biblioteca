package com.example.demo.users.internal.controller.dto;

/** DTO web de salida. Los nombres de campo JSON ({@code nombre}, {@code rol}) no cambian (D16). Sin contraseña. */
public record UserResponse(Long id, String nombre, String email, String rol) {
}
