package com.example.demo.auth.internal.controller.dto;

/** Respuesta del registro. */
public record RegisterResponse(Long id, String name, String email, String role) {
}
