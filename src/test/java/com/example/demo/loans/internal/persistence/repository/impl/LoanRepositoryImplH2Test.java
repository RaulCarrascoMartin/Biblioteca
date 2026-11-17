package com.example.demo.loans.internal.persistence.repository.impl;

import com.example.demo.loans.internal.persistence.repository.mapper.LoanPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Los mismos casos sobre H2 (modo MariaDB) con Flyway: corre sin Docker. */
@DataJpaTest
@ActiveProfiles("test")
@Import({LoanRepositoryImpl.class, LoanPersistenceMapper.class})
class LoanRepositoryImplH2Test implements LoanRepositoryContract {

    @Autowired
    private LoanRepositoryImpl repository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private JdbcTemplate jdbc;

    @Override
    public LoanRepositoryImpl repository() {
        return repository;
    }

    @Override
    public EntityManager entityManager() {
        return entityManager;
    }

    @Override
    public JdbcTemplate jdbc() {
        return jdbc;
    }
}
