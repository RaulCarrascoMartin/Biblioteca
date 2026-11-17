package com.example.demo.users.internal.domain.repository;

import com.example.demo.users.internal.domain.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida del dominio; lo implementa {@code persistence} (inversión de dependencias). */
public interface UserRepository {

    List<User> findAll();

    Optional<User> findById(Long id);

    List<User> findAllById(Collection<Long> ids);

    Optional<User> findByEmail(String email);

    User save(User user);

    void deleteById(Long id);
}
