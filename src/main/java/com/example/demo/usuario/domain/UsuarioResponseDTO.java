package com.example.demo.usuario.domain;

public record UsuarioResponseDTO(Long id, String nombre, String email, String rol) {

    public UsuarioResponseDTO(Usuario usuario) {
        this(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}
