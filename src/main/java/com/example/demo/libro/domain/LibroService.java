package com.example.demo.libro.domain;

import lombok.RequiredArgsConstructor;

import com.example.demo.libro.domain.Libro;
import org.springframework.stereotype.Service;
import com.example.demo.libro.domain.LibroRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final List<LibroEnUsoChecker> enUsoCheckers;

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
        if (enUsoCheckers.stream().anyMatch(c -> c.estaEnUso(id))) {
            throw new IllegalStateException("No se puede retirar un libro con préstamos registrados");
        }
        libroRepository.deleteById(id);
    }

}
