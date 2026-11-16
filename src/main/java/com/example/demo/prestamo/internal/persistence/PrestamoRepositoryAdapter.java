package com.example.demo.prestamo.internal.persistence;

import com.example.demo.prestamo.internal.domain.Prestamo;
import com.example.demo.prestamo.internal.domain.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PrestamoRepositoryAdapter implements PrestamoRepository {

    private final JpaPrestamoRepository jpa;
    private final PrestamoEntityMapper mapper;

    @Override
    public List<Prestamo> findAll() {
        return jpa.findAll().stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<Prestamo> findById(Long id) {
        return jpa.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Prestamo> findByUsuarioId(Long usuarioId) {
        return jpa.findByUsuarioId(usuarioId).stream().map(mapper::toModel).toList();
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
        return mapper.toModel(jpa.save(mapper.toEntity(prestamo)));
    }
}
