package com.example.demo.loans.internal.usecase;

import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.loans.dto.LoanResponseDto;
import com.example.demo.loans.internal.controller.dto.LoanBookResponse;
import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.loans.internal.controller.mapper.LoanWebMapper;
import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.domain.service.LoanService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanUseCaseTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester OWNER = new Requester(2L, Set.of("USER"));
    private static final Requester OTHER = new Requester(3L, Set.of("USER"));
    private static final Requester ANONYMOUS = new Requester(null, Set.of());

    private static final BookResponseDto BOOK = new BookResponseDto(5L, "T", "A", "123", false, Genre.CLASICA);
    private static final LoanResponseDto DTO =
            new LoanResponseDto(10L, LocalDate.of(2026, 1, 2), null, BOOK, 2L, "Ana", "ana@x.es");
    private static final LoanResponse RESPONSE = new LoanResponse(10L, LocalDate.of(2026, 1, 2), null,
            new LoanBookResponse(5L, "T", "A", "123", false, Genre.CLASICA), 2L, "Ana", "ana@x.es");

    @Mock
    private LoanService loanService;

    private final LoanWebMapper webMapper = Mappers.getMapper(LoanWebMapper.class);

    private LoanUseCase useCase() {
        return new LoanUseCase(loanService, webMapper);
    }

    // ---- getAll: ADMIN

    @Test
    void getAll_allowedForAdmin() {
        when(loanService.getAll()).thenReturn(List.of(DTO));

        assertEquals(List.of(RESPONSE), useCase().getAll(ADMIN));
    }

    @Test
    void getAll_forbiddenForNonAdmin() {
        assertThrows(ForbiddenException.class, () -> useCase().getAll(OWNER));

        verifyNoMoreInteractions(loanService);
    }

    // ---- getOwn: authenticated

    @Test
    void getOwn_returnsTheRequestersLoans() {
        when(loanService.getByUserId(2L)).thenReturn(List.of(DTO));

        assertEquals(List.of(RESPONSE), useCase().getOwn(OWNER));
    }

    @Test
    void getOwn_forbiddenWithoutIdentity() {
        assertThrows(ForbiddenException.class, () -> useCase().getOwn(ANONYMOUS));

        verifyNoMoreInteractions(loanService);
    }

    // ---- getById: ADMIN or owner

    @Test
    void getById_allowedForAdmin() {
        when(loanService.getById(10L)).thenReturn(DTO);

        assertEquals(RESPONSE, useCase().getById(ADMIN, 10L));
    }

    @Test
    void getById_allowedForOwner() {
        when(loanService.getById(10L)).thenReturn(DTO);

        assertEquals(RESPONSE, useCase().getById(OWNER, 10L));
    }

    @Test
    void getById_forbiddenForAnotherUser() {
        when(loanService.getById(10L)).thenReturn(DTO);

        assertThrows(ForbiddenException.class, () -> useCase().getById(OTHER, 10L));
    }

    @Test
    void getById_unknownLoanPropagatesNotFound() {
        when(loanService.getById(99L)).thenThrow(new LoanNotFoundException());

        assertThrows(LoanNotFoundException.class, () -> useCase().getById(ADMIN, 99L));
    }

    // ---- create: authenticated, in the requester's name

    @Test
    void create_isMadeInTheRequestersName() {
        when(loanService.create(new CreateLoanDto(5L, 2L))).thenReturn(DTO);

        assertEquals(RESPONSE, useCase().create(OWNER, 5L));

        verify(loanService).create(new CreateLoanDto(5L, 2L));
    }

    @Test
    void create_forbiddenWithoutIdentity() {
        assertThrows(ForbiddenException.class, () -> useCase().create(ANONYMOUS, 5L));

        verifyNoMoreInteractions(loanService);
    }

    // ---- returnLoan: ADMIN or owner

    @Test
    void returnLoan_allowedForAdmin() {
        when(loanService.getById(10L)).thenReturn(DTO);
        when(loanService.returnLoan(10L)).thenReturn(DTO);

        assertEquals(RESPONSE, useCase().returnLoan(ADMIN, 10L));
    }

    @Test
    void returnLoan_allowedForOwner() {
        when(loanService.getById(10L)).thenReturn(DTO);
        when(loanService.returnLoan(10L)).thenReturn(DTO);

        assertEquals(RESPONSE, useCase().returnLoan(OWNER, 10L));
    }

    @Test
    void returnLoan_forbiddenForAnotherUserAndNothingIsReturned() {
        when(loanService.getById(10L)).thenReturn(DTO);

        assertThrows(ForbiddenException.class, () -> useCase().returnLoan(OTHER, 10L));

        verify(loanService).getById(10L);
        verifyNoMoreInteractions(loanService);
    }
}
