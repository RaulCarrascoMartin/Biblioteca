package com.example.demo.persistance.adapter;

import com.example.demo.domain.entity.Libro;
import com.example.demo.domain.port.LibroRepository;
import com.example.demo.persistance.repository.JpaLibroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LibroRepositoryAdapter implements LibroRepository {

    private final JpaLibroRepository jpa;

    @Override
    public List<Libro> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<Libro> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Libro> findByIsbn(String isbn) {
        return jpa.findByIsbn(isbn);
    }

    @Override
    public Optional<Libro> findByIdWithLock(Long id) {
        return jpa.findByIdWithLock(id);
    }

    @Override
    public Libro save(Libro libro) {
        return jpa.save(libro);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
