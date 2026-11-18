package com.example.demo.users.internal.controller.dto;

/** DTO web de salida. Sin contraseña. */
public record UserResponse(Long id, String name, String email, String role) {
}
