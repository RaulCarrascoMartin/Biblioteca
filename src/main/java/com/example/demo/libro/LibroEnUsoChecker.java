package com.example.demo.libro;

/**
 * Punto de extensión (SPI): otros módulos indican si un libro les impide ser retirado.
 * Invierte la dependencia para que {@code libro} no conozca a {@code prestamo}.
 */
public interface LibroEnUsoChecker {

    boolean estaEnUso(Long libroId);
}
