package com.example.demo.catalog.internal.application;

import com.example.demo.catalog.CatalogModule;
import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.internal.domain.service.BookService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/** Implementación de la API pública: solo delega en el servicio, sin política de usuario. */
@Component
public class CatalogModuleImpl implements CatalogModule {

    private final BookService bookService;

    public CatalogModuleImpl(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public Optional<BookResponseDto> getById(Long id) {
        return bookService.findById(id);
    }

    @Override
    public Map<Long, BookResponseDto> getByIds(Collection<Long> ids) {
        return bookService.getByIds(ids);
    }

    @Override
    public BookResponseDto reserve(Long id) {
        return bookService.reserve(id);
    }

    @Override
    public BookResponseDto release(Long id) {
        return bookService.release(id);
    }
}
