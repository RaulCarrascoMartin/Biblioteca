/**
 * Módulo auth: registro, login y logout. Sin API pública. Solo usa la API de users (raíz y DTO) y
 * shared; la seguridad técnica (JWT, filtros, hash) vive en {@code config}.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = {"users", "users::dto", "shared"})
package com.example.demo.auth;
