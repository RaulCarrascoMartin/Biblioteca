package com.example.demo.loans.internal.usecase;

import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.service.LoanService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnLoanUseCaseTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester OWNER = new Requester(2L, Set.of("USER"));
    private static final Requester OTHER = new Requester(3L, Set.of("USER"));
    private static final BookSummary BOOK = new BookSummary(5L, "T", "A", "123", false, Genre.CLASICA);
    private static final LoanDto DTO =
            new LoanDto(10L, LocalDate.of(2026, 1, 2), null, BOOK, 2L, "Ana", "ana@x.es");

    @Mock
    private LoanService loanService;

    @InjectMocks
    private ReturnLoanUseCase useCase;

    @Test
    void allowedForAdmin() {
        when(loanService.getById(10L)).thenReturn(DTO);
        when(loanService.returnLoan(10L)).thenReturn(DTO);

        assertEquals(DTO, useCase.execute(ADMIN, 10L));
    }

    @Test
    void allowedForOwner() {
        when(loanService.getById(10L)).thenReturn(DTO);
        when(loanService.returnLoan(10L)).thenReturn(DTO);

        assertEquals(DTO, useCase.execute(OWNER, 10L));
    }

    @Test
    void forbiddenForAnotherUserAndNothingIsReturned() {
        when(loanService.getById(10L)).thenReturn(DTO);

        assertThrows(ForbiddenException.class, () -> useCase.execute(OTHER, 10L));

        verify(loanService).getById(10L);
        verifyNoMoreInteractions(loanService);
    }
}
