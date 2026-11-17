package com.example.demo.users;

/**
 * Punto de extensión (SPI síncrono): otros módulos indican si un usuario les impide ser eliminado.
 * Invierte la dependencia para que {@code users} no conozca a {@code loans}.
 */
public interface UserInUseChecker {

    boolean isInUse(Long userId);
}
