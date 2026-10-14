package com.example.demo.domain.port;

import com.example.demo.domain.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    List<Usuario> findAll();

    Optional<Usuario> findById(Long id);

    Optional<Usuario> findByEmail(String email);

    Usuario save(Usuario usuario);

    void deleteById(Long id);
}
