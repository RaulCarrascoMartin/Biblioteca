package com.example.demo.usuario.internal.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Modelo de dominio anémico: solo datos, sin persistencia ni HTTP. */
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    private Long id;
    private String nombre;
    private String email;
    private String password;
    private String rol = "USER";

    /** Se incrementa para invalidar todos los tokens emitidos antes (cambio de clave, cierre global). */
    private int tokenVersion = 0;
}
