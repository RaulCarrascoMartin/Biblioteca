package com.example.demo.loans.internal.persistence.repository.impl;

import com.example.demo.loans.internal.domain.exception.LoanConcurrentModificationException;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.loans.internal.domain.repository.LoanRepository;
import com.example.demo.loans.internal.persistence.dao.entity.LoanEntity;
import com.example.demo.loans.internal.persistence.dao.impl.jpa.LoanSpringDataRepository;
import com.example.demo.loans.internal.persistence.repository.mapper.LoanPersistenceMapper;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LoanRepositoryImpl implements LoanRepository {

    private final LoanSpringDataRepository jpa;
    private final LoanPersistenceMapper mapper;

    public LoanRepositoryImpl(LoanSpringDataRepository jpa, LoanPersistenceMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<Loan> findAll() {
        return jpa.findAll().stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<Loan> findById(Long id) {
        return jpa.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Loan> findByUserId(Long userId) {
        return jpa.findByUserId(userId).stream().map(mapper::toModel).toList();
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return jpa.existsByUserId(userId);
    }

    @Override
    public boolean existsByBookId(Long bookId) {
        return jpa.existsByBookId(bookId);
    }

    /** Alta si no tiene id; con id actualiza la entidad gestionada. Traduce el conflicto de {@code @Version}. */
    @Override
    public Loan save(Loan loan) {
        try {
            LoanEntity entity;
            if (loan.id() == null) {
                entity = mapper.toNewEntity(loan);
            } else {
                entity = jpa.findById(loan.id()).orElseThrow(LoanNotFoundException::new);
                mapper.updateManagedEntity(loan, entity);
            }
            LoanEntity managed = jpa.save(entity);
            jpa.flush();
            return mapper.toModel(managed);
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new LoanConcurrentModificationException(e);
        }
    }
}
