package com.example.demo.loans.internal.persistence.repository.impl;

import com.example.demo.loans.internal.persistence.repository.mapper.LoanPersistenceMapper;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import com.example.demo.support.MariaDbContainerTest;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/** Contra MariaDB real con las migraciones Flyway (se omite sin Docker). */
@DataJpaTest(properties = {
        "spring.flyway.locations=classpath:db/migration",
        "spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({LoanRepositoryImpl.class, LoanPersistenceMapper.class})
class LoanRepositoryImplTest  extends MariaDbContainerTest implements LoanRepositoryContract {

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
