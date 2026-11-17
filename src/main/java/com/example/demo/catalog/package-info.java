/**
 * Módulo catalog (catálogo de libros). No depende de otros módulos de negocio; solo de shared
 * (Requester y ForbiddenException, que usa el caso de uso). Su API pública es {@code CatalogModule},
 * {@code catalog.dto} y el SPI {@code catalog.spi}.
 */
@org.springframework.modulith.ApplicationModule(allowedDependencies = "shared")
package com.example.demo.catalog;
