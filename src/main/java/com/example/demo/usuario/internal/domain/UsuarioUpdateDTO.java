package com.example.demo.usuario.internal.domain;

/** Contrato del servicio para actualizar un usuario. Sin validación Jakarta: es del DTO web. */
public record UsuarioUpdateDTO(String nombre, String email, String password) {
}
