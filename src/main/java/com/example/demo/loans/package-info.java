/**
 * Módulo loans (préstamos). Guarda solo ids de libro y usuario; consulta a {@code catalog} y {@code users}
 * por su API pública (raíz, {@code dto} y, en catalog, {@code exception}) y usa {@code shared}
 * (Requester y ForbiddenException, que usa el caso de uso). Su única API pública es {@code loans.dto}.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"catalog", "catalog::dto", "catalog::exception", "users", "users::dto", "shared"}
)
package com.example.demo.loans;
