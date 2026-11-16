package com.example.demo.prestamo.internal.domain;

import com.example.demo.libro.exception.LibroNoDisponibleException;
import com.example.demo.libro.internal.persistence.LibroEntity;
import com.example.demo.prestamo.dto.PrestamoResponseDTO;
import com.example.demo.usuario.internal.persistence.UsuarioEntity;
import com.example.demo.BaseIntegrationTest;
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
        UsuarioEntity ana = crearUsuario("Ana", "ps1@test.es", "USER");
        LibroEntity libro = crearLibro("Libro PS1");

        PrestamoResponseDTO dto = prestamoService.crearPrestamo(libro.getId(), ana.getEmail());

        assertNotNull(dto.id());
        assertEquals(LocalDate.now(), dto.fechaPrestamo());
        assertEquals(null, dto.fechaDevolucion());
        assertEquals(libro.getId(), dto.libro().id());
        assertEquals(libro.getTitulo(), dto.libro().titulo());
        assertEquals(ana.getNombre(), dto.usuarioNombre());
        assertFalse(libroRepository.findById(libro.getId()).orElseThrow().isDisponible());
    }

    @Test
    void noPermitePrestarUnLibroYaPrestado() {
        UsuarioEntity ana = crearUsuario("Ana", "ps2@test.es", "USER");
        LibroEntity libro = crearLibro("Libro PS2");

        prestamoService.crearPrestamo(libro.getId(), ana.getEmail());
        assertThrows(LibroNoDisponibleException.class,
                () -> prestamoService.crearPrestamo(libro.getId(), ana.getEmail()));
    }

    @Test
    void libroInexistenteLanzaError() {
        UsuarioEntity ana = crearUsuario("Ana", "ps3@test.es", "USER");
        assertThrows(RuntimeException.class,
                () -> prestamoService.crearPrestamo(9999L, ana.getEmail()));
    }

    @Test
    void usuarioInexistenteLanzaError() {
        LibroEntity libro = crearLibro("Libro PS4");
        assertThrows(RuntimeException.class,
                () -> prestamoService.crearPrestamo(libro.getId(), "noexiste@test.es"));
    }

    @Test
    void devolverLibroRegistraFechaYVuelveDisponible() {
        UsuarioEntity ana = crearUsuario("Ana", "ps5@test.es", "USER");
        LibroEntity libro = crearLibro("Libro PS5");
        PrestamoResponseDTO prestado = prestamoService.crearPrestamo(libro.getId(), ana.getEmail());

        PrestamoResponseDTO devuelto = prestamoService.devolverLibro(prestado.id());

        assertEquals(LocalDate.now(), devuelto.fechaDevolucion());
        assertTrue(libroRepository.findById(libro.getId()).orElseThrow().isDisponible());
    }

    @Test
    void devolverPrestamoInexistenteLanzaError() {
        assertThrows(RuntimeException.class,
                () -> prestamoService.devolverLibro(9999L));
    }

    @Test
    void obtenerPorEmailAislaLosPrestamosDelUsuario() {
        UsuarioEntity ana = crearUsuario("Ana", "ps6@test.es", "USER");
        UsuarioEntity bob = crearUsuario("Bob", "ps7@test.es", "USER");
        LibroEntity libroA = crearLibro("Libro A");
        LibroEntity libroB = crearLibro("Libro B");

        prestamoService.crearPrestamo(libroA.getId(), ana.getEmail());
        prestamoService.crearPrestamo(libroB.getId(), bob.getEmail());

        var deAna = prestamoService.obtenerPorEmail(ana.getEmail());
        assertEquals(1, deAna.size());
        assertEquals(libroA.getId(), deAna.get(0).libro().id());

        assertEquals(1, prestamoService.obtenerPorEmail(bob.getEmail()).size());
        assertTrue(prestamoService.obtenerPorEmail("desconocido@test.es").isEmpty());
    }
}