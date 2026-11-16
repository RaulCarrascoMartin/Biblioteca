package com.example.demo.usuario;

/**
 * Punto de extensión (SPI): otros módulos indican si un usuario les impide ser eliminado.
 * Invierte la dependencia para que {@code usuario} no conozca a {@code prestamo}.
 */
public interface UsuarioEnUsoChecker {

    boolean estaEnUso(Long usuarioId);
}
