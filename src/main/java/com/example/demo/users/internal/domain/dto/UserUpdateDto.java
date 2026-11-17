package com.example.demo.users.internal.domain.dto;

/** Contrato del servicio para actualizar un usuario. Sin validación Jakarta: es del DTO web. */
public record UserUpdateDto(String name, String email, String password) {
}
