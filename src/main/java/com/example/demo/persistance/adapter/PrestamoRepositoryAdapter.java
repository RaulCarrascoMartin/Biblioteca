package com.example.demo.persistance.adapter;

import com.example.demo.domain.entity.Prestamo;
import com.example.demo.domain.port.PrestamoRepository;
import com.example.demo.persistance.repository.JpaPrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PrestamoRepositoryAdapter implements PrestamoRepository {

    private final JpaPrestamoRepository jpa;

    @Override
    public List<Prestamo> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<Prestamo> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public List<Prestamo> findByUsuarioId(Long usuarioId) {
        return jpa.findByUsuarioId(usuarioId);
    }

    @Override
    public boolean existsByUsuarioId(Long usuarioId) {
        return jpa.existsByUsuarioId(usuarioId);
    }

    @Override
    public boolean existsByLibroId(Long libroId) {
        return jpa.existsByLibroId(libroId);
    }

    @Override
    public Prestamo save(Prestamo prestamo) {
        return jpa.save(prestamo);
    }
}
