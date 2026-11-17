/**
 * Módulo auth: registro, login, refresh y logout. Sin API pública. Solo usa la API de users (raíz y DTO)
 * y shared; la seguridad técnica (JWT, filtros, hash) vive en {@code config}. Guarda los refresh tokens
 * (hash, caducidad y revocación) en su propia persistencia.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"users", "users::dto", "shared"})
package com.example.demo.auth;
