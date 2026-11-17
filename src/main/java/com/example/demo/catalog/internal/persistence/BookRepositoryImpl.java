package com.example.demo.catalog.internal.persistence;

import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.repository.BookRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final JpaBookRepository jpa;
    private final BookEntityMapper mapper;

    public BookRepositoryImpl(JpaBookRepository jpa, BookEntityMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public List<Book> findAll() {
        return jpa.findAll().stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return jpa.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Book> findAllById(Collection<Long> ids) {
        return jpa.findAllById(ids).stream().map(mapper::toModel).toList();
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return jpa.findByIsbn(isbn).map(mapper::toModel);
    }

    @Override
    public Optional<Book> findByIdWithLock(Long id) {
        return jpa.findByIdWithLock(id).map(mapper::toModel);
    }

    @Override
    public Book save(Book book) {
        return mapper.toModel(jpa.save(mapper.toEntity(book)));
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
