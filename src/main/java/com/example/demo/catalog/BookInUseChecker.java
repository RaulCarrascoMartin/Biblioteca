package com.example.demo.catalog;

/**
 * SPI síncrono: otro módulo (p. ej. {@code loans}) lo implementa para vetar la retirada de un libro
 * que tiene en uso. {@code catalog} consulta a todas las implementaciones antes de borrar.
 */
public interface BookInUseChecker {

    boolean isInUse(Long bookId);
}
