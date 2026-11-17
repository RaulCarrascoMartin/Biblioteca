package com.example.demo.shared;

import java.util.List;

/**
 * Identidad autenticada que sale de un token de acceso ya validado: el id del usuario ({@code sub}) y
 * los roles del claim {@code roles} (con prefijo {@code ROLE_}, p. ej. {@code ROLE_USER}). Son los
 * roles de la emisión: no se consulta la base de datos.
 */
public record AuthenticatedPrincipal(Long userId, List<String> roles) {

    public AuthenticatedPrincipal {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        roles = roles == null ? List.of() : List.copyOf(roles);
    }
}
