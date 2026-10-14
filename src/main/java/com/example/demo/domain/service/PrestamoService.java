package com.example.demo.domain.service;


import com.example.demo.domain.dto.PrestamoResponseDTO;
import com.example.demo.domain.entity.Libro;
import com.example.demo.domain.entity.Prestamo;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.domain.exception.NotFoundException;
import com.example.demo.persistance.repository.LibroRepository;
import com.example.demo.persistance.repository.PrestamoRepository;
import com.example.demo.persistance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<Prestamo> obtenerTodos(){
        return prestamoRepository.findAll();
    }

    public Optional<Prestamo> obtenerPorId(Long id){
        return prestamoRepository.findById(id);
    }

    public List<Prestamo> obtenerPorEmail(String emailUsuario){
        return usuarioRepository.findByEmail(emailUsuario)
                .map(usuario -> prestamoRepository.findByUsuarioId(usuario.getId()))
                .orElseGet(List::of);
    }

    @Transactional
    public PrestamoResponseDTO crearPrestamo(Long libroId, String emailUsuario){
        Libro libro = libroRepository.findByIdWithLock(libroId)
                .orElseThrow(() -> new NotFoundException("Libro no encontrado"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new NotFoundException("El usuario no existe"));

        if (!libro.isDisponible()){
            throw new IllegalStateException("El libro no se encuentra disponible en estos momentos");
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
            throw new IllegalStateException("El préstamo ya fue devuelto");
        }

        prestamo.setFechaDevolucion(LocalDate.now());
        libro.setDisponible(true);

        return new PrestamoResponseDTO(prestamoRepository.save(prestamo));
    }

}