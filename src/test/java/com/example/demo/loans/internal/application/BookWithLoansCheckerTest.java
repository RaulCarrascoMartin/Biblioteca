package com.example.demo.loans.internal.application;

import com.example.demo.loans.internal.domain.service.LoanQueryService;
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
    private LoanQueryService loanQueryService;
    @InjectMocks
    private BookWithLoansChecker checker;

    @Test
    void isInUseWhenTheBookHasLoans() {
        when(loanQueryService.existsByBookId(1L)).thenReturn(true);

        assertTrue(checker.isInUse(1L));
    }

    @Test
    void isNotInUseWhenTheBookHasNoLoans() {
        when(loanQueryService.existsByBookId(2L)).thenReturn(false);

        assertFalse(checker.isInUse(2L));
    }
}
