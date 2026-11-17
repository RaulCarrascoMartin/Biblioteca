package com.example.demo.loans.internal.domain.service;

import com.example.demo.catalog.CatalogModule;
import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.catalog.dto.ReservationResult;
import com.example.demo.loans.internal.domain.exception.LoanBookNotFoundException;
import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.exception.LoanBookAlreadyAvailableException;
import com.example.demo.loans.internal.domain.exception.LoanBookNotAvailableException;
import com.example.demo.loans.internal.domain.exception.LoanNotFoundException;
import com.example.demo.loans.internal.domain.exception.LoanUserNotFoundException;
import com.example.demo.loans.internal.domain.mapper.LoanMapper;
import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.loans.internal.domain.repository.LoanRepository;
import com.example.demo.users.UsersModule;
import com.example.demo.users.dto.UserSummary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lógica de préstamos. Solo conoce a {@code catalog} y {@code users} a través de su API pública
 * ({@link CatalogModule}, {@link UsersModule} y DTO); guarda ids y nunca toca sus repositorios ni modelos.
 */
@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;
    private final CatalogModule catalogModule;
    private final UsersModule usersModule;

    public LoanServiceImpl(LoanRepository loanRepository, LoanMapper loanMapper,
                           CatalogModule catalogModule, UsersModule usersModule) {
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
        this.catalogModule = catalogModule;
        this.usersModule = usersModule;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDto> getAll() {
        return toDtos(loanRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public LoanDto getById(Long id) {
        return loanRepository.findById(id).map(this::toDto).orElseThrow(LoanNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanDto> getByUserId(Long userId) {
        return toDtos(loanRepository.findByUserId(userId));
    }

    @Override
    @Transactional
    public LoanDto create(CreateLoanDto dto) {
        UserSummary user = usersModule.getById(dto.userId()).orElseThrow(LoanUserNotFoundException::new);

        // Bloquea el libro y comprueba su disponibilidad (BookNotFound / BookNotAvailable)
        BookSummary book = requireReserved(catalogModule.reserve(dto.bookId()));

        Loan loan = Loan.open(dto.bookId(), dto.userId(), LocalDate.now());

        return loanMapper.toDto(loanRepository.save(loan), book, user);
    }

    @Override
    @Transactional
    public LoanDto returnLoan(Long id) {
        Loan loan = loanRepository.findById(id).orElseThrow(LoanNotFoundException::new);

        // Bloquea el libro para serializar con create. Si el préstamo ya estaba devuelto, markReturned lanza
        // y la transacción se revierte, deshaciendo también la liberación. Una devolución concurrente que
        // se colara entre la lectura y el bloqueo la detecta el @Version del préstamo al guardar
        // (LoanConcurrentModificationException) y también revierte la liberación.
        BookSummary book = requireReleased(catalogModule.release(loan.bookId()));

        Loan returned = loan.markReturned(LocalDate.now());

        UserSummary user = usersModule.getById(returned.userId()).orElseThrow(LoanUserNotFoundException::new);
        return loanMapper.toDto(loanRepository.save(returned), book, user);
    }

    private LoanDto toDto(Loan loan) {
        BookSummary book = catalogModule.getById(loan.bookId()).orElseThrow(LoanBookNotFoundException::new);
        UserSummary user = usersModule.getById(loan.userId()).orElseThrow(LoanUserNotFoundException::new);
        return loanMapper.toDto(loan, book, user);
    }

    /** Resuelve libros y usuarios en bloque (2 consultas) en lugar de una por préstamo. */
    private List<LoanDto> toDtos(List<Loan> loans) {
        if (loans.isEmpty()) {
            return List.of();
        }
        Set<Long> bookIds = loans.stream().map(Loan::bookId).collect(Collectors.toSet());
        Set<Long> userIds = loans.stream().map(Loan::userId).collect(Collectors.toSet());
        Map<Long, BookSummary> books = catalogModule.getByIds(bookIds);
        Map<Long, UserSummary> users = usersModule.getByIds(userIds);

        return loans.stream()
                .map(loan -> loanMapper.toDto(loan,
                        requireBook(books, loan.bookId()), requireUser(users, loan.userId())))
                .toList();
    }

    private static BookSummary requireReserved(ReservationResult result) {
        return switch (result.status()) {
            case OK -> result.book();
            case NOT_FOUND -> throw new LoanBookNotFoundException();
            case NOT_AVAILABLE, ALREADY_AVAILABLE -> throw new LoanBookNotAvailableException();
        };
    }

    private static BookSummary requireReleased(ReservationResult result) {
        return switch (result.status()) {
            case OK -> result.book();
            case NOT_FOUND -> throw new LoanBookNotFoundException();
            case ALREADY_AVAILABLE, NOT_AVAILABLE -> throw new LoanBookAlreadyAvailableException();
        };
    }

    private static BookSummary requireBook(Map<Long, BookSummary> books, Long id) {
        BookSummary book = books.get(id);
        if (book == null) {
            throw new LoanBookNotFoundException();
        }
        return book;
    }

    private static UserSummary requireUser(Map<Long, UserSummary> users, Long id) {
        UserSummary user = users.get(id);
        if (user == null) {
            throw new LoanUserNotFoundException();
        }
        return user;
    }
}
