package com.example.demo.users.internal.persistence.repository.impl;

import com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException;
import com.example.demo.users.internal.domain.exception.UserNotFoundException;
import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.domain.repository.UserRepository;
import com.example.demo.users.internal.persistence.dao.entity.UserEntity;
import com.example.demo.users.internal.persistence.dao.impl.jpa.UserSpringDataRepository;
import com.example.demo.users.internal.persistence.repository.mapper.UserPersistenceMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserSpringDataRepository jpa;
    private final UserPersistenceMapper mapper;

    public UserRepositoryImpl(UserSpringDataRepository jpa, UserPersistenceMapper mapper) {
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
        UserEntity entity;
        if (user.id() == null) {
            entity = mapper.toNewEntity(user);
        } else {
            entity = jpa.findById(user.id()).orElseThrow(() -> new UserNotFoundException(user.id()));
            mapper.updateManagedEntity(user, entity);
        }
        try {
            UserEntity managed = jpa.save(entity);
            jpa.flush();
            return mapper.toModel(managed);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyRegisteredException();
        }
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
