package com.example.demo.prestamo.domain;

import lombok.RequiredArgsConstructor;


import com.example.demo.libro.domain.Libro;
import com.example.demo.usuario.domain.Usuario;
import com.example.demo.shared.NotFoundException;
import com.example.demo.libro.domain.LibroRepository;
import com.example.demo.usuario.domain.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> obtenerTodos(){
        return prestamoRepository.findAll().stream().map(PrestamoResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Optional<PrestamoResponseDTO> obtenerPorId(Long id){
        return prestamoRepository.findById(id).map(PrestamoResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponseDTO> obtenerPorEmail(String emailUsuario){
        return usuarioRepository.findByEmail(emailUsuario)
                .map(usuario -> prestamoRepository.findByUsuarioId(usuario.getId()))
                .orElseGet(List::of)
                .stream().map(PrestamoResponseDTO::new).toList();
    }

    @Transactional
    public PrestamoResponseDTO crearPrestamo(Long libroId, String emailUsuario){
        Libro libro = libroRepository.findByIdWithLock(libroId)
                .orElseThrow(() -> new NotFoundException("Libro no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("El usuario no existe"));

        if (!libro.isDisponible()){
            throw new LibroNoDisponibleException();
        }

        libro.setDisponible(false);

        Prestamo prestamo = new Prestamo();
        prestamo.setLibro(libro);
        prestamo.setUsuario(usuario);
        prestamo.setFechaPrestamo(LocalDate.now());

        return new PrestamoResponseDTO(prestamoRepository.save(prestamo));
    }

    @Transactional
    public PrestamoResponseDTO devolverLibro(Long prestamoId){
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new NotFoundException("Prestamo no encontrado"));

        // Bloquea el libro para serializar con crearPrestamo y revalida tras el bloqueo
        Libro libro = libroRepository.findByIdWithLock(prestamo.getLibro().getId())
                .orElseThrow(() -> new NotFoundException("Libro no encontrado"));

        if (prestamo.getFechaDevolucion() != null) {
            throw new PrestamoYaDevueltoException();
        }

        prestamo.setFechaDevolucion(LocalDate.now());
        libro.setDisponible(true);

        return new PrestamoResponseDTO(prestamoRepository.save(prestamo));
    }

}