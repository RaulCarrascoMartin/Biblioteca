package com.example.demo.domain.dto;


import com.example.demo.domain.entity.Usuario;
import lombok.Data;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private String rol;


    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.email = usuario.getEmail();
        this.rol = usuario.getRol();
    }

}
