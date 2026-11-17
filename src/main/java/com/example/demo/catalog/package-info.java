/**
 * Módulo catalog (catálogo de libros). No depende de otros módulos de negocio; solo de shared
 * (Requester y ForbiddenException, que usa el caso de uso).
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = "shared")
package com.example.demo.catalog;
