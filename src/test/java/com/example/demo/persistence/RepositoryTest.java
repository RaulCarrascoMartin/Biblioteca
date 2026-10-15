package com.example.demo.persistence;

import com.example.demo.libro.domain.Libro;
import com.example.demo.prestamo.domain.Prestamo;
import com.example.demo.usuario.domain.Usuario;
import com.example.demo.libro.persistence.JpaLibroRepository;
import com.example.demo.prestamo.persistence.JpaPrestamoRepository;
import com.example.demo.usuario.persistence.JpaUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class RepositoryTest {

    @Autowired
    private JpaUsuarioRepository usuarioRepository;

    @Autowired
    private JpaLibroRepository libroRepository;

    @Autowired
    private JpaPrestamoRepository prestamoRepository;

    private Usuario usuario(String email) {
        Usuario u = new Usuario();
        u.setNombre("Un Lector");
        u.setEmail(email);
        u.setPassword("{noop}x");
        u.setRol("USER");
        return usuarioRepository.saveAndFlush(u);
    }

    private Libro libro(String isbn) {
        Libro l = new Libro();
        l.setTitulo("Titulo " + isbn);
        l.setAutor("Autor");
        l.setIsbn(isbn);
        return libroRepository.saveAndFlush(l);
    }

    private Prestamo prestamo(Usuario u, Libro l, LocalDate fechaDevolucion) {
        Prestamo p = new Prestamo();
        p.setUsuario(u);
        p.setLibro(l);
        p.setFechaPrestamo(LocalDate.of(2026, 1, 10));
        p.setFechaDevolucion(fechaDevolucion);
        return prestamoRepository.saveAndFlush(p);
    }

    @Test
    void findByEmailEncuentraYSoloUsuariosExistentes() {
        usuario("r1@test.es");
        assertTrue(usuarioRepository.findByEmail("r1@test.es").isPresent());
        assertTrue(usuarioRepository.findByEmail("noexiste@test.es").isEmpty());
    }

    @Test
    void emailDuplicadoViolaLasReglas() {
        usuario("dup@test.es");
        Usuario repetido = new Usuario();
        repetido.setNombre("Otro Lector");
        repetido.setEmail("dup@test.es");
        repetido.setPassword("{noop}x");
        repetido.setRol("USER");
        assertThrows(DataIntegrityViolationException.class,
                () -> usuarioRepository.saveAndFlush(repetido));
    }

    @Test
    void findByIsbnEncuentraSOLosExistentes() {
        libro("9781111111117");
        assertTrue(libroRepository.findByIsbn("9781111111117").isPresent());
        assertTrue(libroRepository.findByIsbn("9789999999999").isEmpty());
    }

    @Test
    void findByUsuarioIdDevuelveSoloLosPrestamosDelUsuario() {
        Usuario ana = usuario("ana-repo@test.es");
        Usuario bob = usuario("bob-repo@test.es");
        libro("9782222222224");
        libro("9783333333331");
        Libro libroAna = libro("9784444444448");
        prestamo(ana, libroAna, null);
        prestamo(bob, libro("9785555555555"), null);
        prestamo(ana, libro("9786666666662"), LocalDate.of(2026, 2, 1));

        var deAna = prestamoRepository.findByUsuarioId(ana.getId());
        assertEquals(2, deAna.size());
        assertTrue(deAna.stream().allMatch(p -> p.getUsuario().getId().equals(ana.getId())));
    }

    @Test
    void findByFechaDevolucionIsNullDevuelveSoloActivos() {
        Usuario ana = usuario("activa@test.es");
        prestamo(ana, libro("9787777777779"), null);
        prestamo(ana, libro("9788888888886"), LocalDate.of(2026, 2, 1));

        var activos = prestamoRepository.findByFechaDevolucionIsNull();
        assertEquals(1, activos.size());
        assertFalse(activos.get(0).getLibro().getIsbn().equals("9788888888886"));
    }
}