package com.example.demo.loans.internal.domain.repository;

import com.example.demo.loans.internal.domain.model.Loan;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del dominio; lo implementa {@code persistence} (inversión de dependencias). */
public interface LoanRepository {

    List<Loan> findAll();

    Optional<Loan> findById(Long id);

    List<Loan> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByBookId(Long bookId);

    Loan save(Loan loan);
}
