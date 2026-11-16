package com.example.demo.libro.internal.persistence;

import com.example.demo.libro.internal.domain.Libro;
import com.example.demo.libro.internal.domain.LibroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LibroRepositoryAdapter implements LibroRepository {

    private final JpaLibroRepository jpa;
    private final LibroEntityMapper mapper;

    @Override
    public List<Libro> findAll() {
        return jpa.findAll().stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<Libro> findById(Long id) {
        return jpa.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Libro> findAllById(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<Libro> findByIsbn(String isbn) {
        return jpa.findByIsbn(isbn).map(mapper::toModel);
    }

    @Override
    public Optional<Libro> findByIdWithLock(Long id) {
        return jpa.findByIdWithLock(id).map(mapper::toModel);
    }

    @Override
    public Libro save(Libro libro) {
        return mapper.toModel(jpa.save(mapper.toEntity(libro)));
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
