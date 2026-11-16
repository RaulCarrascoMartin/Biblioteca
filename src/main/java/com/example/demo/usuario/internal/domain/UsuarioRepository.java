package com.example.demo.usuario.internal.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida del dominio; lo implementa {@code persistence} (inversión de dependencias). */
public interface UsuarioRepository {
    List<Usuario> findAll();

    Optional<Usuario> findById(Long id);

    List<Usuario> findAllById(Collection<Long> ids);

    Optional<Usuario> findByEmail(String email);

    Usuario save(Usuario usuario);

    void deleteById(Long id);
}
