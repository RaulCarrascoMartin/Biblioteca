package com.example.demo.libro.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final List<LibroEnUsoChecker> enUsoCheckers;

    @Transactional(readOnly = true)
    public List<LibroResponseDTO> obtenerTodos() {
        return libroRepository.findAll().stream().map(LibroResponseDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public Optional<LibroResponseDTO> obtenerPorId(Long id) {
        return libroRepository.findById(id).map(LibroResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<LibroResponseDTO> obtenerPorIsbn(String isbn) {
        return libroRepository.findByIsbn(isbn).map(LibroResponseDTO::new);
    }

    @Transactional
    public LibroResponseDTO crear(LibroDTO dto) {
        if (libroRepository.findByIsbn(dto.isbn()).isPresent()) {
            throw new IsbnDuplicadoException();
        }

        Libro libro = new Libro();
        libro.setTitulo(dto.titulo());
        libro.setAutor(dto.autor());
        libro.setIsbn(dto.isbn());
        libro.setGenero(dto.genero());
        libro.setDisponible(true);

        return new LibroResponseDTO(libroRepository.save(libro));
    }

    /** Devuelve vacío si el libro no existe. El género nulo no sobrescribe el actual. */
    @Transactional
    public Optional<LibroResponseDTO> actualizar(Long id, LibroDTO dto) {
        return libroRepository.findById(id).map(libro -> {
            Optional<Libro> duplicado = libroRepository.findByIsbn(dto.isbn());
            if (duplicado.isPresent() && !duplicado.get().getId().equals(id)) {
                throw new IsbnDuplicadoException();
            }

            libro.setTitulo(dto.titulo());
            libro.setAutor(dto.autor());
            libro.setIsbn(dto.isbn());
            if (dto.genero() != null) {
                libro.setGenero(dto.genero());
            }

            return new LibroResponseDTO(libroRepository.save(libro));
        });
    }

    @Transactional
    public void eliminar(Long id) {
        if (enUsoCheckers.stream().anyMatch(c -> c.estaEnUso(id))) {
            throw new LibroConPrestamosException();
        }
        libroRepository.deleteById(id);
    }

}
