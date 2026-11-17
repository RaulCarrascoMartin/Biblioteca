package com.example.demo.loans.internal.domain.service;

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
class LoanQueryServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @InjectMocks
    private LoanQueryServiceImpl service;

    @Test
    void existsDelegateToTheRepository() {
        when(loanRepository.existsByBookId(2L)).thenReturn(true);
        when(loanRepository.existsByUserId(3L)).thenReturn(false);

        assertTrue(service.existsByBookId(2L));
        assertFalse(service.existsByUserId(3L));
    }
}
