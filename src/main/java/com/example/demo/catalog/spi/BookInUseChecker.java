package com.example.demo.catalog.spi;

/**
 * SPI síncrono: otro módulo (p. ej. {@code loans}) lo implementa para vetar la retirada de un libro
 * que tiene en uso. {@code catalog} consulta a todas las implementaciones antes de borrar.
 *
 * <p>Excepción documentada a la comunicación por eventos ({@code structure.md}): se usa un veto
 * síncrono, no un evento, porque el resultado forma parte de la consistencia del flujo de borrado
 * (si hay préstamos el borrado no debe ocurrir) y debe decidirse dentro de la misma transacción
 * (ver {@code eventos.md}, apartado 1).
 */
public interface BookInUseChecker {

    boolean isInUse(Long bookId);
}
