package com.example.demo.users.internal.domain.dto;

/** Contrato del servicio: vista de un usuario sin contraseña. */
public record UserDto(Long id, String name, String email, String role) {
}
