package com.example.demo.catalog.internal.domain.service;

import com.example.demo.catalog.BookInUseChecker;
import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.exception.BookNotAvailableException;
import com.example.demo.catalog.exception.BookNotFoundException;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.dto.SeedBooksDto;
import com.example.demo.catalog.internal.domain.exception.BookWithLoansException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
import com.example.demo.catalog.internal.domain.mapper.BookMapper;
import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final List<BookInUseChecker> inUseCheckers;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper,
                           List<BookInUseChecker> inUseCheckers) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.inUseCheckers = inUseCheckers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDto> getAll() {
        return bookRepository.findAll().stream().map(bookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookResponseDto> findById(Long id) {
        return bookRepository.findById(id).map(bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDto getById(Long id) {
        return findById(id).orElseThrow(BookNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, BookResponseDto> getByIds(Collection<Long> ids) {
        return bookRepository.findAllById(ids).stream()
                .map(bookMapper::toResponse)
                .collect(Collectors.toMap(BookResponseDto::id, Function.identity()));
    }

    @Override
    @Transactional
    public BookResponseDto create(SaveBookDto dto) {
        if (bookRepository.findByIsbn(dto.isbn()).isPresent()) {
            throw new DuplicateIsbnException();
        }
        Book book = bookMapper.toModel(dto);
        book.setAvailable(true);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponseDto update(Long id, SaveBookDto dto) {
        Book book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);
        Optional<Book> duplicate = bookRepository.findByIsbn(dto.isbn());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new DuplicateIsbnException();
        }
        bookMapper.update(dto, book);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponseDto reserve(Long id) {
        Book book = bookRepository.findByIdWithLock(id).orElseThrow(BookNotFoundException::new);
        if (!book.isAvailable()) {
            throw new BookNotAvailableException();
        }
        book.setAvailable(false);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookResponseDto release(Long id) {
        Book book = bookRepository.findByIdWithLock(id).orElseThrow(BookNotFoundException::new);
        if (book.isAvailable()) {
            throw new BookAlreadyAvailableException();
        }
        book.setAvailable(true);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (bookRepository.findById(id).isEmpty()) {
            throw new BookNotFoundException();
        }
        if (inUseCheckers.stream().anyMatch(checker -> checker.isInUse(id))) {
            throw new BookWithLoansException();
        }
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public SeedBooksDto seed(List<SaveBookDto> catalog) {
        int created = 0;
        int updated = 0;
        for (SaveBookDto seed : catalog) {
            Optional<Book> existing = bookRepository.findByIsbn(seed.isbn());
            if (existing.isEmpty()) {
                Book book = bookMapper.toModel(seed);
                book.setAvailable(true);
                bookRepository.save(book);
                created++;
            } else if (existing.get().getGenre() != seed.genre()) {
                Book book = existing.get();
                book.setGenre(seed.genre());
                bookRepository.save(book);
                updated++;
            }
        }
        return new SeedBooksDto(created, updated);
    }
}
