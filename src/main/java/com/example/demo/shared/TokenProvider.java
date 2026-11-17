package com.example.demo.shared;

import java.util.Optional;

/** Puerto técnico: emisión y validación de tokens. La implementación (JWT) vive en {@code config}. */
public interface TokenProvider {

    String generate(String email, int version);

    /** Devuelve los datos del token, o vacío si está manipulado, caducado o no es de esta aplicación. */
    Optional<TokenClaims> validate(String token);
}
