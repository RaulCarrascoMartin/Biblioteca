package com.example.demo.users.spi;

/**
 * Punto de extensión (SPI síncrono): otros módulos indican si un usuario les impide ser eliminado.
 * Es un veto síncrono, no un evento, porque el resultado condiciona el borrado (ver el
 * {@code package-info} de este paquete). Invierte la dependencia para que {@code users} no conozca a
 * {@code loans}.
 */
public interface UserInUseChecker {

    boolean isInUse(Long userId);
}
