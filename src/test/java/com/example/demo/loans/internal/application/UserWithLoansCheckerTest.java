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
class UserWithLoansCheckerTest {

    @Mock
    private LoanRepository loanRepository;
    @InjectMocks
    private UserWithLoansChecker checker;

    @Test
    void isInUseWhenTheUserHasLoans() {
        when(loanRepository.existsByUserId(1L)).thenReturn(true);

        assertTrue(checker.isInUse(1L));
    }

    @Test
    void isNotInUseWhenTheUserHasNoLoans() {
        when(loanRepository.existsByUserId(2L)).thenReturn(false);

        assertFalse(checker.isInUse(2L));
    }
}
