/**
 * Composición transversal de la aplicación; ningún módulo de negocio depende de ella. Subpaquetes:
 * {@code security} (JWT, filtros, hash), {@code web} (resolver de Requester y manejo global de errores)
 * y {@code startup} (carga inicial).
 */
@org.springframework.modulith.ApplicationModule(type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.example.demo.config;
