package com.example.demo.loans.internal.domain.service;

import com.example.demo.loans.internal.domain.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanQueryServiceImpl implements LoanQueryService {

    private final LoanRepository loanRepository;

    public LoanQueryServiceImpl(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByBookId(Long bookId) {
        return loanRepository.existsByBookId(bookId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        return loanRepository.existsByUserId(userId);
    }
}
