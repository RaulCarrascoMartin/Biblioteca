package com.example.demo.auth.internal.domain.dto;

/** Usuario recién registrado, sin contraseña. */
public record RegisteredUserDto(Long id, String name, String email, String role) {
}
