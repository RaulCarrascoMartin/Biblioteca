package com.example.demo.loans.internal.persistence.dao.impl.jpa;

import com.example.demo.loans.internal.persistence.dao.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanSpringDataRepository extends JpaRepository<LoanEntity, Long> {

    List<LoanEntity> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    boolean existsByBookId(Long bookId);
}
