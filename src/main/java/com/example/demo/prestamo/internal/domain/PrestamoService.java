package com.example.demo.prestamo.internal.domain;

import com.example.demo.libro.LibroModule;
import com.example.demo.libro.dto.LibroResponseDTO;
import com.example.demo.prestamo.dto.PrestamoResponseDTO;
import com.example.demo.prestamo.internal.domain.exception.PrestamoYaDevueltoException;
import com.example.demo.shared.NotFoundException;
import com.example.demo.usuario.UsuarioModule;
import com.example.demo.usuario.dto.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lógica de préstamos. Solo conoce a {@code libro} y {@code usuario} a través de su API pública
 * ({@code LibroModule}, {@code UsuarioModule} y DTO); guarda sus ids y nunca toca sus repositorios ni sus modelos.
 */
@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final PrestamoMapper prestamoMapper;
    private final LibroModule libroModule;
    private final UsuarioModule usuarioModule;

    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> obtenerTodos(){
        return aRespuestas(prestamoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<PrestamoResponseDTO> obtenerPorId(Long id){
        return prestamoRepository.findById(id).map(this::aRespuesta);
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> obtenerPorEmail(String emailUsuario){
        return usuarioModule.obtenerPorEmail(emailUsuario)
                .map(usuario -> prestamoRepository.findByUsuarioId(usuario.id()))
                .map(this::aRespuestas)
                .orElseGet(List::of);
    }

    @Transactional(readOnly = true)
    public boolean esDueño(Long prestamoId, String email){
        if (email == null) {
            return false;
        }
        return prestamoRepository.findById(prestamoId)
                .flatMap(prestamo -> usuarioModule.obtenerPorId(prestamo.getUsuarioId()))
                .map(usuario -> email.equalsIgnoreCase(usuario.email()))
                .orElse(false);
    }

    @Transactional
    public PrestamoResponseDTO crearPrestamo(Long libroId, String emailUsuario){
        UsuarioResponseDTO usuario = usuarioModule.obtenerPorEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("El usuario no existe"));

        // Bloquea el libro y comprueba su disponibilidad (NotFound / LibroNoDisponible)
        LibroResponseDTO libro = libroModule.reservar(libroId);

        Prestamo prestamo = new Prestamo();
        prestamo.setLibroId(libro.id());
        prestamo.setUsuarioId(usuario.id());
        prestamo.setFechaPrestamo(LocalDate.now());

        return prestamoMapper.toResponse(prestamoRepository.save(prestamo), libro, usuario);
    }

    @Transactional
    public PrestamoResponseDTO devolverLibro(Long prestamoId){
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new NotFoundException("Prestamo no encontrado"));

        // Bloquea el libro para serializar con crearPrestamo y revalida tras el bloqueo
        LibroResponseDTO libro = libroModule.liberar(prestamo.getLibroId());

        if (prestamo.getFechaDevolucion() != null) {
            throw new PrestamoYaDevueltoException();
        }

        prestamo.setFechaDevolucion(LocalDate.now());

        UsuarioResponseDTO usuario = usuarioDe(prestamo);
        return prestamoMapper.toResponse(prestamoRepository.save(prestamo), libro, usuario);
    }

    private PrestamoResponseDTO aRespuesta(Prestamo prestamo) {
        LibroResponseDTO libro = libroModule.obtenerPorId(prestamo.getLibroId())
                .orElseThrow(() -> new NotFoundException("Libro no encontrado"));
        return prestamoMapper.toResponse(prestamo, libro, usuarioDe(prestamo));
    }

    private UsuarioResponseDTO usuarioDe(Prestamo prestamo) {
        return usuarioModule.obtenerPorId(prestamo.getUsuarioId())
                .orElseThrow(() -> new NotFoundException("El usuario no existe"));
    }

    /** Resuelve libros y usuarios en bloque (2 consultas) en lugar de una por préstamo. */
    private List<PrestamoResponseDTO> aRespuestas(List<Prestamo> prestamos) {
        Set<Long> libroIds = prestamos.stream().map(Prestamo::getLibroId).collect(Collectors.toSet());
        Set<Long> usuarioIds = prestamos.stream().map(Prestamo::getUsuarioId).collect(Collectors.toSet());
        Map<Long, LibroResponseDTO> libros = libroModule.obtenerPorIds(libroIds);
        Map<Long, UsuarioResponseDTO> usuarios = usuarioModule.obtenerPorIds(usuarioIds);

        return prestamos.stream()
                .map(p -> prestamoMapper.toResponse(p, libros.get(p.getLibroId()), usuarios.get(p.getUsuarioId())))
                .toList();
    }
}
