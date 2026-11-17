package com.example.demo.loans.internal.application;

import com.example.demo.loans.internal.domain.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookWithLoansCheckerTest {

    @Mock
    private LoanRepository loanRepository;
    @InjectMocks
    private BookWithLoansChecker checker;

    @Test
    void isInUseWhenTheBookHasLoans() {
        when(loanRepository.existsByBookId(1L)).thenReturn(true);

        assertTrue(checker.isInUse(1L));
    }

    @Test
    void isNotInUseWhenTheBookHasNoLoans() {
        when(loanRepository.existsByBookId(2L)).thenReturn(false);

        assertFalse(checker.isInUse(2L));
    }
}
