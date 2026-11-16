package com.example.demo.persistence;

import com.example.demo.libro.internal.persistence.JpaLibroRepository;
import com.example.demo.libro.internal.persistence.LibroEntity;
import com.example.demo.prestamo.internal.persistence.JpaPrestamoRepository;
import com.example.demo.prestamo.internal.persistence.PrestamoEntity;
import com.example.demo.usuario.internal.persistence.JpaUsuarioRepository;
import com.example.demo.usuario.internal.persistence.UsuarioEntity;
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

    private UsuarioEntity usuario(String email) {
        UsuarioEntity u = new UsuarioEntity();
        u.setNombre("Un Lector");
        u.setEmail(email);
        u.setPassword("{noop}x");
        u.setRol("USER");
        return usuarioRepository.saveAndFlush(u);
    }

    private LibroEntity libro(String isbn) {
        LibroEntity l = new LibroEntity();
        l.setTitulo("Titulo " + isbn);
        l.setAutor("Autor");
        l.setIsbn(isbn);
        return libroRepository.saveAndFlush(l);
    }

    private PrestamoEntity prestamo(UsuarioEntity u, LibroEntity l, LocalDate fechaDevolucion) {
        PrestamoEntity p = new PrestamoEntity();
        p.setUsuarioId(u.getId());
        p.setLibroId(l.getId());
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
        UsuarioEntity repetido = new UsuarioEntity();
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
        UsuarioEntity ana = usuario("ana-repo@test.es");
        UsuarioEntity bob = usuario("bob-repo@test.es");
        libro("9782222222224");
        libro("9783333333331");
        LibroEntity libroAna = libro("9784444444448");
        prestamo(ana, libroAna, null);
        prestamo(bob, libro("9785555555555"), null);
        prestamo(ana, libro("9786666666662"), LocalDate.of(2026, 2, 1));

        var deAna = prestamoRepository.findByUsuarioId(ana.getId());
        assertEquals(2, deAna.size());
        assertTrue(deAna.stream().allMatch(p -> p.getUsuarioId().equals(ana.getId())));
    }

    @Test
    void findByFechaDevolucionIsNullDevuelveSoloActivos() {
        UsuarioEntity ana = usuario("activa@test.es");
        prestamo(ana, libro("9787777777779"), null);
        prestamo(ana, libro("9788888888886"), LocalDate.of(2026, 2, 1));

        var activos = prestamoRepository.findByFechaDevolucionIsNull();
        assertEquals(1, activos.size());
        assertTrue(activos.get(0).getFechaDevolucion() == null);
    }
}