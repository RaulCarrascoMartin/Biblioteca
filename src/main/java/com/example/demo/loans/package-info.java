/**
 * Módulo loans (préstamos). No publica API: no tiene {@code LoansModule} ni DTO públicos porque ningún otro
 * módulo lo consume por llamada directa. Guarda solo ids de libro y usuario; consulta a {@code catalog} y
 * {@code users} por su API pública (raíz, {@code dto} y {@code spi}) y usa {@code shared} (Requester y
 * ForbiddenException, que usa el caso de uso). Hacia fuera solo implementa los vetos síncronos
 * {@code catalog.spi.BookInUseChecker} y {@code users.spi.UserInUseChecker} (excepción documentada a los
 * eventos, {@code eventos.md} §1).
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"catalog", "catalog::dto", "catalog::spi", "users", "users::dto", "users::spi", "shared"}
)
package com.example.demo.loans;
