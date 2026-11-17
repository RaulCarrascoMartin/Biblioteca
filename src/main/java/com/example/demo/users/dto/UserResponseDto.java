package com.example.demo.users.dto;

/** Vista pública de un usuario. Nunca incluye la contraseña. */
public record UserResponseDto(Long id, String name, String email, String role) {
}
