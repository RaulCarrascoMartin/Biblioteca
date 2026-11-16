package com.example.demo.usuario.internal.persistence;

import com.example.demo.usuario.internal.domain.Usuario;
import com.example.demo.usuario.internal.domain.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final JpaUsuarioRepository jpa;
    private final UsuarioEntityMapper mapper;

    @Override
    public List<Usuario> findAll() {
        return jpa.findAll().stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpa.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Usuario> findAllById(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toModel);
    }

    @Override
    public Usuario save(Usuario usuario) {
        return mapper.toModel(jpa.save(mapper.toEntity(usuario)));
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
