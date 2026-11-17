package com.example.demo.loans.internal.domain.service;

import com.example.demo.catalog.CatalogModule;
import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.exception.BookNotAvailableException;
import com.example.demo.catalog.exception.BookNotFoundException;
import com.example.demo.loans.dto.LoanResponseDto;
import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
import com.example.demo.loans.internal.domain.exception.LoanAlreadyReturnedException;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.domain.exception.LoanUserNotFoundException;
import com.example.demo.loans.internal.domain.mapper.LoanMapper;
import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.loans.internal.domain.repository.LoanRepository;
import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.UserResponseDto;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private static final BookResponseDto BOOK = new BookResponseDto(2L, "Libro A", "Autor", "123", false, Genre.CLASICA);
    private static final UserResponseDto ANA = new UserResponseDto(3L, "Ana", "ana@x.es", "USER");

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
        Loan loan = new Loan();
        loan.setId(id);
        loan.setBookId(bookId);
        loan.setUserId(userId);
        loan.setLoanDate(LocalDate.of(2026, 1, 2));
        loan.setReturnDate(returnDate);
        return loan;
    }

    // ---- create

    @Test
    void createReservesTheBookAndSavesTheLoanInTheUsersName() {
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));
        when(catalogModule.reserve(2L)).thenReturn(BOOK);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> {
            Loan saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        LoanResponseDto result = service.create(new CreateLoanDto(2L, 3L));

        ArgumentCaptor<Loan> captor = ArgumentCaptor.forClass(Loan.class);
        verify(loanRepository).save(captor.capture());
        assertEquals(2L, captor.getValue().getBookId());
        assertEquals(3L, captor.getValue().getUserId());
        assertEquals(LocalDate.now(), captor.getValue().getLoanDate());
        assertNull(captor.getValue().getReturnDate());
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
        when(catalogModule.reserve(2L)).thenThrow(new BookNotFoundException());

        assertThrows(BookNotFoundException.class, () -> service.create(new CreateLoanDto(2L, 3L)));

        verify(loanRepository, never()).save(any());
    }

    @Test
    void createPropagatesBookNotAvailable() {
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));
        when(catalogModule.reserve(2L)).thenThrow(new BookNotAvailableException());

        assertThrows(BookNotAvailableException.class, () -> service.create(new CreateLoanDto(2L, 3L)));

        verify(loanRepository, never()).save(any());
    }

    // ---- returnLoan

    @Test
    void returnLoanReleasesTheBookAndRecordsTheDate() {
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan(10L, 2L, 3L, null)));
        when(catalogModule.release(2L)).thenReturn(BOOK);
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        LoanResponseDto result = service.returnLoan(10L);

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
        when(catalogModule.release(2L)).thenReturn(BOOK);

        assertThrows(LoanAlreadyReturnedException.class, () -> service.returnLoan(10L));

        verify(loanRepository, never()).save(any());
    }

    @Test
    void returnLoanPropagatesBookAlreadyAvailable() {
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan(10L, 2L, 3L, null)));
        when(catalogModule.release(2L)).thenThrow(new BookAlreadyAvailableException());

        assertThrows(BookAlreadyAvailableException.class, () -> service.returnLoan(10L));

        verify(loanRepository, never()).save(any());
    }

    // ---- queries

    @Test
    void getByIdComposesBookAndUser() {
        when(loanRepository.findById(10L)).thenReturn(Optional.of(loan(10L, 2L, 3L, null)));
        when(catalogModule.getById(2L)).thenReturn(Optional.of(BOOK));
        when(usersModule.getById(3L)).thenReturn(Optional.of(ANA));

        LoanResponseDto result = service.getById(10L);

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

        List<LoanResponseDto> result = service.getAll();

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

        assertThrows(BookNotFoundException.class, () -> service.getAll());
    }

    @Test
    void getByUserIdIsolatesTheUsersLoans() {
        when(loanRepository.findByUserId(3L)).thenReturn(List.of(loan(1L, 2L, 3L, null)));
        when(catalogModule.getByIds(Set.of(2L))).thenReturn(Map.of(2L, BOOK));
        when(usersModule.getByIds(Set.of(3L))).thenReturn(Map.of(3L, ANA));

        List<LoanResponseDto> result = service.getByUserId(3L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).book().id());
    }

    @Test
    void getByUserIdWithoutLoansIsEmpty() {
        when(loanRepository.findByUserId(5L)).thenReturn(List.of());

        assertTrue(service.getByUserId(5L).isEmpty());
    }

    @Test
    void existsDelegateToTheRepository() {
        when(loanRepository.existsByBookId(2L)).thenReturn(true);
        when(loanRepository.existsByUserId(3L)).thenReturn(false);

        assertTrue(service.existsByBookId(2L));
        assertFalse(service.existsByUserId(3L));
    }
}
