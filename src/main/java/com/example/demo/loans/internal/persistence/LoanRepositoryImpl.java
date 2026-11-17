package com.example.demo.loans.internal.persistence;

import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.loans.internal.domain.repository.LoanRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LoanRepositoryImpl implements LoanRepository {

    private final JpaLoanRepository jpa;
    private final LoanEntityMapper mapper;

    public LoanRepositoryImpl(JpaLoanRepository jpa, LoanEntityMapper mapper) {
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

    @Override
    public Loan save(Loan loan) {
        return mapper.toModel(jpa.save(mapper.toEntity(loan)));
    }
}
