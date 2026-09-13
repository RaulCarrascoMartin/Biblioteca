package com.example.demo.domain.service;

import com.example.demo.BaseIntegrationTest;
import com.example.demo.domain.dto.PrestamoResponseDTO;
import com.example.demo.domain.entity.Libro;
import com.example.demo.domain.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrestamoServiceTest extends BaseIntegrationTest {

    @Autowired
    private PrestamoService prestamoService;

    @BeforeEach
    void setUp() {
        limpiarBD();
    }

    @Test
    void crearPrestamoMarcaElLibroComoNoDisponible() {
        Usuario ana = crearUsuario("Ana", "ps1@test.es", "USER");
        Libro libro = crearLibro("Libro PS1");

        PrestamoResponseDTO dto = prestamoService.crearPrestamo(libro.getId(), ana.getEmail());

        assertNotNull(dto.getId());
        assertEquals(LocalDate.now(), dto.getFechaPrestamo());
        assertEquals(null, dto.getFechaDevolucion());
        assertEquals(libro.getId(), dto.getLibro().getId());
        assertEquals(ana.getNombre(), dto.getUsuarioNombre());
        assertFalse(libroRepository.findById(libro.getId()).orElseThrow().isDisponible());
    }

    @Test
    void noPermitePrestarUnLibroYaPrestado() {
        Usuario ana = crearUsuario("Ana", "ps2@test.es", "USER");
        Libro libro = crearLibro("Libro PS2");

        prestamoService.crearPrestamo(libro.getId(), ana.getEmail());
        assertThrows(IllegalStateException.class,
                () -> prestamoService.crearPrestamo(libro.getId(), ana.getEmail()));
    }

    @Test
    void libroInexistenteLanzaError() {
        Usuario ana = crearUsuario("Ana", "ps3@test.es", "USER");
        assertThrows(RuntimeException.class,
                () -> prestamoService.crearPrestamo(9999L, ana.getEmail()));
    }

    @Test
    void usuarioInexistenteLanzaError() {
        Libro libro = crearLibro("Libro PS4");
        assertThrows(RuntimeException.class,
                () -> prestamoService.crearPrestamo(libro.getId(), "noexiste@test.es"));
    }

    @Test
    void devolverLibroRegistraFechaYVuelveDisponible() {
        Usuario ana = crearUsuario("Ana", "ps5@test.es", "USER");
        Libro libro = crearLibro("Libro PS5");
        PrestamoResponseDTO prestado = prestamoService.crearPrestamo(libro.getId(), ana.getEmail());

        PrestamoResponseDTO devuelto = prestamoService.devolverLibro(prestado.getId());

        assertEquals(LocalDate.now(), devuelto.getFechaDevolucion());
        assertTrue(libroRepository.findById(libro.getId()).orElseThrow().isDisponible());
    }

    @Test
    void devolverPrestamoInexistenteLanzaError() {
        assertThrows(RuntimeException.class,
                () -> prestamoService.devolverLibro(9999L));
    }

    @Test
    void obtenerPorEmailAislaLosPrestamosDelUsuario() {
        Usuario ana = crearUsuario("Ana", "ps6@test.es", "USER");
        Usuario bob = crearUsuario("Bob", "ps7@test.es", "USER");
        Libro libroA = crearLibro("Libro A");
        Libro libroB = crearLibro("Libro B");

        prestamoService.crearPrestamo(libroA.getId(), ana.getEmail());
        prestamoService.crearPrestamo(libroB.getId(), bob.getEmail());

        var deAna = prestamoService.obtenerPorEmail(ana.getEmail());
        assertEquals(1, deAna.size());
        assertEquals(libroA.getId(), deAna.get(0).getLibro().getId());

        assertEquals(1, prestamoService.obtenerPorEmail(bob.getEmail()).size());
        assertTrue(prestamoService.obtenerPorEmail("desconocido@test.es").isEmpty());
    }
}