package com.example.demo.catalog.internal.domain.service;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.exception.BookNotFoundException;
import com.example.demo.catalog.internal.domain.exception.BookWithLoansException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
import com.example.demo.catalog.internal.domain.mapper.BookMapper;
import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.model.Isbn;
import com.example.demo.catalog.internal.domain.repository.BookRepository;
import com.example.demo.catalog.spi.BookInUseChecker;
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
    public List<BookDto> getAll() {
        return bookRepository.findAll().stream().map(bookMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookDto> findById(Long id) {
        return bookRepository.findById(id).map(bookMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto getById(Long id) {
        return findById(id).orElseThrow(BookNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, BookDto> getByIds(Collection<Long> ids) {
        return bookRepository.findAllById(ids).stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toMap(BookDto::id, Function.identity()));
    }

    @Override
    @Transactional
    public BookDto create(SaveBookDto dto) {
        Book book = bookMapper.toModel(dto);
        if (bookRepository.findByIsbn(book.isbn().value()).isPresent()) {
            throw new DuplicateIsbnException();
        }
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    @Transactional
    public BookDto update(Long id, SaveBookDto dto) {
        Book book = bookRepository.findById(id).orElseThrow(BookNotFoundException::new);
        Isbn isbn = new Isbn(dto.isbn());
        Optional<Book> duplicate = bookRepository.findByIsbn(isbn.value());
        if (duplicate.isPresent() && !duplicate.get().id().equals(id)) {
            throw new DuplicateIsbnException();
        }
        Book updated = book.withDetails(dto.title(), dto.author(), isbn, dto.genre());
        return bookMapper.toDto(bookRepository.save(updated));
    }

    @Override
    @Transactional
    public BookDto reserve(Long id) {
        Book book = bookRepository.findByIdWithLock(id).orElseThrow(BookNotFoundException::new);
        return bookMapper.toDto(bookRepository.save(book.reserve()));
    }

    @Override
    @Transactional
    public BookDto release(Long id) {
        Book book = bookRepository.findByIdWithLock(id).orElseThrow(BookNotFoundException::new);
        return bookMapper.toDto(bookRepository.save(book.release()));
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
}
