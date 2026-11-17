package com.example.demo.auth.internal.controller.dto;

/** Respuesta del registro. Nombres de campo en español: contrato histórico con el frontend (D16). */
public record RegisterResponse(Long id, String nombre, String email, String rol) {
}
