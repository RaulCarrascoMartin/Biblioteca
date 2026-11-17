package com.example.demo.users.internal.persistence;

import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JpaUserRepository jpa;
    private final UserEntityMapper mapper;

    public UserRepositoryImpl(JpaUserRepository jpa, UserEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<User> findAll() {
        return jpa.findAll().stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpa.findById(id).map(mapper::toModel);
    }

    @Override
    public List<User> findAllById(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(mapper::toModel);
    }

    @Override
    public User save(User user) {
        return mapper.toModel(jpa.save(mapper.toEntity(user)));
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
