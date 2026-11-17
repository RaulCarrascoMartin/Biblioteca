package com.example.demo.loans.internal.domain.service;

import com.example.demo.catalog.CatalogModule;
import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.dto.ReservationResult;
import com.example.demo.loans.internal.domain.exception.LoanBookAlreadyAvailableException;
import com.example.demo.loans.internal.domain.exception.LoanConcurrentModificationException;
import com.example.demo.loans.internal.domain.exception.LoanBookNotAvailableException;
import com.example.demo.loans.internal.domain.exception.LoanBookNotFoundException;
import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.domain.exception.LoanUserNotFoundException;
import com.example.demo.loans.internal.domain.mapper.LoanMapper;
import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.loans.internal.domain.repository.LoanRepository;
import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    private static final BookSummary BOOK = new BookSummary(2L, "Libro A", "Autor", "123", false, Genre.CLASICA);
    private static final UserSummary ANA = new UserSummary(3L, "Ana", "ana@x.es", "USER");

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private CatalogModule catalogModule;
    @Mock
    private UsersModule usersModule;

    private LoanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LoanServiceImpl(loanRepository, Mappers.getMapper(LoanMapper.class), catalogModule, usersModule);
    }

    private static Loan loan(Long id, Long bookId, Long userId, LocalDate returnDate) {
        return new Loan(id, bookId, userId, LocalDate.of(2026, 1, 2), returnDate);
    }

    // ---- create

    @Test
    void createReservesTheBookAndSavesTheLoanInTheUsersName() {
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));
        when(catalogModule.reserve(2L)).thenReturn(ReservationResult.ok(BOOK));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan saved = inv.getArgument(0);
            return new Loan(10L, saved.bookId(), saved.userId(), saved.loanDate(), saved.returnDate());
        });

        LoanDto result = service.create(new CreateLoanDto(2L, 3L));

        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(captor.capture());
        assertEquals(2L, captor.getValue().bookId());
        assertEquals(3L, captor.getValue().userId());
        assertEquals(LocalDate.now(), captor.getValue().loanDate());
        assertNull(captor.getValue().returnDate());
        assertEquals(10L, result.id());
        assertEquals(BOOK, result.book());
        assertEquals("Ana", result.userName());
        assertEquals("ana@x.es", result.userEmail());
        assertEquals(3L, result.userId());
    }

    @Test
    void createWithUnknownUserFailsBeforeReservingTheBook() {
        when(usersModule.getById(3L)).thenReturn(Optional.empty());

        assertThrows(LoanUserNotFoundException.class, () -> service.create(new CreateLoanDto(2L, 3L)));

        verifyNoInteractions(catalogModule);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void createPropagatesBookNotFound() {
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));
        when(catalogModule.reserve(2L)).thenReturn(ReservationResult.of(ReservationResult.Status.NOT_FOUND));

        assertThrows(LoanBookNotFoundException.class, () -> service.create(new CreateLoanDto(2L, 3L)));

        verify(loanRepository, never()).save(any());
    }

    @Test
    void createPropagatesBookNotAvailable() {
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));
        when(catalogModule.reserve(2L)).thenReturn(ReservationResult.of(ReservationResult.Status.NOT_AVAILABLE));

        assertThrows(LoanBookNotAvailableException.class, () -> service.create(new CreateLoanDto(2L, 3L)));

        verify(loanRepository, never()).save(any());
    }

    // ---- returnLoan

    @Test
    void returnLoanReleasesTheBookAndRecordsTheDate() {
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan(10L, 2L, 3L, null)));
        when(catalogModule.release(2L)).thenReturn(ReservationResult.ok(BOOK));
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanDto result = service.returnLoan(10L);

        assertEquals(LocalDate.now(), result.returnDate());
        verify(catalogModule).release(2L);
    }

    @Test
    void returnLoanUnknownIsNotFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> service.returnLoan(99L));

        verifyNoInteractions(catalogModule);
    }

    @Test
    void returnLoanAlreadyReturnedFailsAndSavesNothing() {
        when(loanRepository.findById(10L))
                .thenReturn(Optional.of(loan(10L, 2L, 3L, LocalDate.of(2026, 1, 5))));
        when(catalogModule.release(2L)).thenReturn(ReservationResult.ok(BOOK));

        assertThrows(LoanAlreadyReturnedException.class, () -> service.returnLoan(10L));

        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoanPropagatesBookAlreadyAvailable() {
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan(10L, 2L, 3L, null)));
        when(catalogModule.release(2L)).thenReturn(ReservationResult.of(ReservationResult.Status.ALREADY_AVAILABLE));

        assertThrows(LoanBookAlreadyAvailableException.class, () -> service.returnLoan(10L));

        verify(loanRepository, never()).save(any());
    }

    // ---- queries

    @Test
    void getByIdComposesBookAndUser() {
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan(10L, 2L, 3L, null)));
        when(catalogModule.getById(2L)).thenReturn(Optional.of(BOOK));
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));

        LoanDto result = service.getById(10L);

        assertEquals(10L, result.id());
        assertEquals(BOOK, result.book());
        assertEquals(3L, result.userId());
    }

    @Test
    void getByIdUnknownIsNotFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void getAllResolvesBooksAndUsersInBulk() {
        when(loanRepository.findAll()).thenReturn(List.of(loan(1L, 2L, 3L, null), loan(2L, 2L, 3L, null)));
        when(catalogModule.getByIds(Set.of(2L))).thenReturn(Map.of(2L, BOOK));
        when(usersModule.getByIds(Set.of(3L))).thenReturn(Map.of(3L, ANA));

        List<LoanDto> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals("Ana", result.get(1).userName());
        verify(catalogModule).getByIds(Set.of(2L));
        verify(usersModule).getByIds(Set.of(3L));
        verify(catalogModule, never()).getById(any());
        verify(usersModule, never()).getById(any());
    }

    @Test
    void getAllWithoutLoansDoesNotCallOtherModules() {
        when(loanRepository.findAll()).thenReturn(List.of());

        assertTrue(service.getAll().isEmpty());

        verifyNoInteractions(catalogModule, usersModule);
    }

    @Test
    void getAllFailsWhenABookIsMissing() {
        when(loanRepository.findAll()).thenReturn(List.of(loan(1L, 2L, 3L, null)));
        when(catalogModule.getByIds(Set.of(2L))).thenReturn(Map.of());
        when(usersModule.getByIds(Set.of(3L))).thenReturn(Map.of(3L, ANA));

        assertThrows(LoanBookNotFoundException.class, () -> service.getAll());
    }

    @Test
    void getByUserIdIsolatesTheUsersLoans() {
        when(loanRepository.findByUserId(3L)).thenReturn(List.of(loan(1L, 2L, 3L, null)));
        when(catalogModule.getByIds(Set.of(2L))).thenReturn(Map.of(2L, BOOK));
        when(usersModule.getByIds(Set.of(3L))).thenReturn(Map.of(3L, ANA));

        List<LoanDto> result = service.getByUserId(3L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).book().id());
    }

    @Test
    void getByUserIdWithoutLoansIsEmpty() {
        when(loanRepository.findByUserId(5L)).thenReturn(List.of());

        assertTrue(service.getByUserId(5L).isEmpty());
    }

    @Test
    void returnLoanPropagatesTheConcurrentModificationAndDoesNotSwallowIt() {
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan(10L, 2L, 3L, null)));
        when(catalogModule.release(2L)).thenReturn(ReservationResult.ok(BOOK));
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));
        when(loanRepository.save(any(Loan.class))).thenThrow(new LoanConcurrentModificationException(null));

        assertThrows(LoanConcurrentModificationException.class, () -> service.returnLoan(10L));
    }
}
