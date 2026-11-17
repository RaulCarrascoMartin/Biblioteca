package com.example.demo.loans.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaLoanRepository extends JpaRepository<LoanEntity, Long> {

    List<LoanEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByBookId(Long bookId);
}
