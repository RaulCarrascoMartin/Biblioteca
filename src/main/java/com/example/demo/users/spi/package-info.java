/**
 * Punto de extensión (SPI) del módulo {@code users}.
 *
 * <p>Excepción documentada a la comunicación inversa por eventos: {@link
 * com.example.demo.users.spi.UserInUseChecker} es un veto síncrono. Otro módulo (hoy {@code loans}) la
 * implementa para impedir la eliminación de un usuario, y la respuesta forma parte de la consistencia
 * del flujo (el borrado debe fallar antes de ejecutarse). Un evento asíncrono no podría vetar la
 * operación (ver {@code eventos.md} §1). Invierte la dependencia: {@code users} no conoce a quien lo
 * implementa.
 */
@org.springframework.modulith.NamedInterface("spi")
package com.example.demo.users.spi;
