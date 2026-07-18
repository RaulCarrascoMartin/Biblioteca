package com.example.demo.domain.service;


import com.example.demo.domain.entity.Libro;
import com.example.demo.domain.entity.Prestamo;
import com.example.demo.domain.entity.Usuario;
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

    @Transactional
    public Prestamo crearPrestamo(Long libroId, Long usuarioId){
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));

        if (!libro.isDisponible()){
            throw new IllegalStateException("El libro no se encuentra disponible en estos momentos");
        }

        libro.setDisponible(false);
        libroRepository.save(libro);

        Prestamo prestamo = new Prestamo();
        prestamo.setLibro(libro);
        prestamo.setUsuario(usuario);
        prestamo.setFechaPrestamo(LocalDate.now());

        return prestamoRepository.save(prestamo);
    }

    @Transactional
    public Prestamo devolverLibro(Long prestamoId){
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new RuntimeException("Prestamo no encontrado"));

        prestamo.setFechaDevolucion(LocalDate.now());

        Libro libro = prestamo.getLibro();
        libro.setDisponible(true);
        libroRepository.save(libro);

        return prestamoRepository.save(prestamo);
    }

}
