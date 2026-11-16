/** Módulo de autenticación: login, registro, JWT y seguridad. */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"usuario", "usuario::dto", "shared"}
)
package com.example.demo.auth;
