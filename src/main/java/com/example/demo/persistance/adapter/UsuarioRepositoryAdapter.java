package com.example.demo.persistance.adapter;

import com.example.demo.domain.entity.Usuario;
import com.example.demo.domain.port.UsuarioRepository;
import com.example.demo.persistance.repository.JpaUsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final JpaUsuarioRepository jpa;

    @Override
    public List<Usuario> findAll() {
        return jpa.findAll();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return jpa.findByEmail(email);
    }

    @Override
    public Usuario save(Usuario usuario) {
        return jpa.save(usuario);
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
