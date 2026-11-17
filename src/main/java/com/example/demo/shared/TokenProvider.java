package com.example.demo.shared;

import java.util.List;
import java.util.Optional;

/** Puerto técnico: emisión y validación del token de acceso. La implementación (JWT) vive en {@code config}. */
public interface TokenProvider {

    /** Emite un token de acceso de vida corta para el usuario, con sus roles (p. ej. {@code ROLE_USER}). */
    String generate(Long userId, List<String> roles);

    /** Devuelve la identidad del token, o vacío si está manipulado, caducado, sin firma o no es de esta aplicación. */
    Optional<AuthenticatedPrincipal> validate(String token);
}
