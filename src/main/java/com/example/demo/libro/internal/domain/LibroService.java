package com.example.demo.libro.internal.domain;

import com.example.demo.libro.LibroEnUsoChecker;
import com.example.demo.libro.dto.LibroDTO;
import com.example.demo.libro.dto.LibroResponseDTO;
import com.example.demo.libro.internal.domain.exception.IsbnDuplicadoException;
import com.example.demo.libro.internal.domain.exception.LibroConPrestamosException;
import com.example.demo.libro.exception.LibroNoDisponibleException;
import com.example.demo.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final LibroMapper libroMapper;
    private final List<LibroEnUsoChecker> enUsoCheckers;

    @Transactional(readOnly = true)
    public List<LibroResponseDTO> obtenerTodos() {
        return libroRepository.findAll().stream().map(libroMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<LibroResponseDTO> obtenerPorId(Long id) {
        return libroRepository.findById(id).map(libroMapper::toResponse);
    }

    @Transactional
    public LibroResponseDTO crear(LibroDTO dto) {
        if (libroRepository.findByIsbn(dto.isbn()).isPresent()) {
            throw new IsbnDuplicadoException();
        }

        Libro libro = libroMapper.toModel(dto);
        libro.setDisponible(true);

        return libroMapper.toResponse(libroRepository.save(libro));
    }

    /** Devuelve vacío si el libro no existe. El género nulo no sobrescribe el actual. */
    @Transactional
    public Optional<LibroResponseDTO> actualizar(Long id, LibroDTO dto) {
        return libroRepository.findById(id).map(libro -> {
            Optional<Libro> duplicado = libroRepository.findByIsbn(dto.isbn());
            if (duplicado.isPresent() && !duplicado.get().getId().equals(id)) {
                throw new IsbnDuplicadoException();
            }

            libroMapper.actualizar(dto, libro);

            return libroMapper.toResponse(libroRepository.save(libro));
        });
    }

    /** Varios libros de una vez (evita N consultas), indexados por id. Los ids inexistentes se omiten. */
    @Transactional(readOnly = true)
    public Map<Long, LibroResponseDTO> obtenerPorIds(Collection<Long> ids) {
        return libroRepository.findAllById(ids).stream()
                .map(libroMapper::toResponse)
                .collect(Collectors.toMap(LibroResponseDTO::id, Function.identity()));
    }

    /** API para otros módulos: marca el libro como no disponible (con bloqueo) al prestarlo. */
    @Transactional
    public LibroResponseDTO reservar(Long id) {
        Libro libro = libroRepository.findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("Libro no encontrado"));
        if (!libro.isDisponible()) {
            throw new LibroNoDisponibleException();
        }
        libro.setDisponible(false);
        return libroMapper.toResponse(libroRepository.save(libro));
    }

    /** API para otros módulos: vuelve a dejar el libro disponible (con bloqueo) al devolverlo. */
    @Transactional
    public LibroResponseDTO liberar(Long id) {
        Libro libro = libroRepository.findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("Libro no encontrado"));
        libro.setDisponible(true);
        return libroMapper.toResponse(libroRepository.save(libro));
    }

    @Transactional
    public void eliminar(Long id) {
        if (enUsoCheckers.stream().anyMatch(c -> c.estaEnUso(id))) {
            throw new LibroConPrestamosException();
        }
        libroRepository.deleteById(id);
    }

}
