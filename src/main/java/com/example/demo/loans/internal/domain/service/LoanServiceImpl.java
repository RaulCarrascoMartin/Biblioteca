package com.example.demo.loans.internal.domain.service;

import com.example.demo.catalog.CatalogModule;
import com.example.demo.catalog.dto.BookResponseDto;
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
    public List<LoanResponseDto> getAll() {
        return toResponses(loanRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public LoanResponseDto getById(Long id) {
        return loanRepository.findById(id).map(this::toResponse).orElseThrow(LoanNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanResponseDto> getByUserId(Long userId) {
        return toResponses(loanRepository.findByUserId(userId));
    }

    @Override
    @Transactional
    public LoanResponseDto create(CreateLoanDto dto) {
        UserResponseDto user = usersModule.getById(dto.userId()).orElseThrow(LoanUserNotFoundException::new);

        // Bloquea el libro y comprueba su disponibilidad (BookNotFound / BookNotAvailable)
        BookResponseDto book = catalogModule.reserve(dto.bookId());

        Loan loan = loanMapper.toModel(dto);
        loan.setLoanDate(LocalDate.now());

        return loanMapper.toResponse(loanRepository.save(loan), book, user);
    }

    @Override
    @Transactional
    public LoanResponseDto returnLoan(Long id) {
        Loan loan = loanRepository.findById(id).orElseThrow(LoanNotFoundException::new);

        // Bloquea el libro para serializar con create y revalida tras el bloqueo. Si el préstamo ya estaba
        // devuelto, la excepción siguiente revierte la transacción y deshace también la liberación.
        BookResponseDto book = catalogModule.release(loan.getBookId());

        if (loan.getReturnDate() != null) {
            throw new LoanAlreadyReturnedException();
        }
        loan.setReturnDate(LocalDate.now());

        UserResponseDto user = usersModule.getById(loan.getUserId()).orElseThrow(LoanUserNotFoundException::new);
        return loanMapper.toResponse(loanRepository.save(loan), book, user);
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

    private LoanResponseDto toResponse(Loan loan) {
        BookResponseDto book = catalogModule.getById(loan.getBookId()).orElseThrow(BookNotFoundException::new);
        UserResponseDto user = usersModule.getById(loan.getUserId()).orElseThrow(LoanUserNotFoundException::new);
        return loanMapper.toResponse(loan, book, user);
    }

    /** Resuelve libros y usuarios en bloque (2 consultas) en lugar de una por préstamo. */
    private List<LoanResponseDto> toResponses(List<Loan> loans) {
        if (loans.isEmpty()) {
            return List.of();
        }
        Set<Long> bookIds = loans.stream().map(Loan::getBookId).collect(Collectors.toSet());
        Set<Long> userIds = loans.stream().map(Loan::getUserId).collect(Collectors.toSet());
        Map<Long, BookResponseDto> books = catalogModule.getByIds(bookIds);
        Map<Long, UserResponseDto> users = usersModule.getByIds(userIds);

        return loans.stream()
                .map(loan -> loanMapper.toResponse(loan,
                        requireBook(books, loan.getBookId()), requireUser(users, loan.getUserId())))
                .toList();
    }

    private static BookResponseDto requireBook(Map<Long, BookResponseDto> books, Long id) {
        BookResponseDto book = books.get(id);
        if (book == null) {
            throw new BookNotFoundException();
        }
        return book;
    }

    private static UserResponseDto requireUser(Map<Long, UserResponseDto> users, Long id) {
        UserResponseDto user = users.get(id);
        if (user == null) {
            throw new LoanUserNotFoundException();
        }
        return user;
    }
}
