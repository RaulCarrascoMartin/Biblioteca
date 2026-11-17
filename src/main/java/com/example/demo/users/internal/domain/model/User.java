package com.example.demo.users.internal.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Modelo de dominio anémico: solo datos, sin persistencia ni HTTP. */
@Getter
@Setter
@NoArgsConstructor
public class User {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String role = "USER";

    /** Se incrementa para invalidar todos los tokens emitidos antes (cambio de clave, cierre global). */
    private int tokenVersion = 0;
}
