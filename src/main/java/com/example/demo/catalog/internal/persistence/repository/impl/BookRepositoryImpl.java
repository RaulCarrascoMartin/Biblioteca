package com.example.demo.catalog.internal.persistence.repository.impl;

import com.example.demo.catalog.internal.domain.exception.BookNotFoundException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.repository.BookRepository;
import com.example.demo.catalog.internal.persistence.dao.entity.BookEntity;
import com.example.demo.catalog.internal.persistence.dao.impl.jpa.BookSpringDataRepository;
import com.example.demo.catalog.internal.persistence.repository.mapper.BookPersistenceMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final BookSpringDataRepository jpa;
    private final BookPersistenceMapper mapper;

    public BookRepositoryImpl(BookSpringDataRepository jpa, BookPersistenceMapper mapper) {
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
        try {
            BookEntity entity;
            if (book.id() == null) {
                entity = mapper.toNewEntity(book);
            } else {
                entity = jpa.findById(book.id()).orElseThrow(BookNotFoundException::new);
                mapper.updateManagedEntity(book, entity);
            }
            BookEntity managed = jpa.save(entity);
            jpa.flush();
            return mapper.toModel(managed);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateIsbnException();
        }
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
