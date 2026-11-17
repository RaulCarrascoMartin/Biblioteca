package com.example.demo.users.internal.persistence.dao.impl.jpa;

import com.example.demo.users.internal.persistence.dao.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSpringDataRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
}
