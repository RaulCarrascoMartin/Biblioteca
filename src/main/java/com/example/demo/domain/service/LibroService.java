package com.example.demo.domain.service;

import com.example.demo.domain.entity.Libro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.persistance.repository.LibroRepository;
import com.example.demo.persistance.repository.PrestamoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    public List<Libro>obtenerTodos(){
        return libroRepository.findAll();
    }

    public Optional<Libro>obtenerPorId(Long id){
        return libroRepository.findById(id);
    }

    public Optional<Libro>obtenerPorIsbn(String isbn){
        return libroRepository.findByIsbn(isbn);
    }

    public Libro guardar(Libro libro){
        return libroRepository.save(libro);
    }

    public void eliminar(Long id){
        if (prestamoRepository.existsByLibroId(id)) {
            throw new IllegalStateException("No se puede retirar un libro con préstamos registrados");
        }
        libroRepository.deleteById(id);
    }

}
