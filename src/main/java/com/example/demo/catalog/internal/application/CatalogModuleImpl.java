package com.example.demo.catalog.internal.application;

import com.example.demo.catalog.CatalogModule;
import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.dto.ReservationResult;
import com.example.demo.catalog.dto.ReservationResult.Status;
import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotFoundException;
import com.example.demo.catalog.internal.domain.service.BookService;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Implementación de la API pública: delega en el servicio y mapea {@link BookDto} a DTO público. */
@Component
public class CatalogModuleImpl implements CatalogModule {

    private final BookService bookService;

    public CatalogModuleImpl(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public Optional<BookSummary> getById(Long id) {
        return bookService.findById(id).map(CatalogModuleImpl::toSummary);
    }

    @Override
    public Map<Long, BookSummary> getByIds(Collection<Long> ids) {
        return bookService.getByIds(ids).values().stream()
                .map(CatalogModuleImpl::toSummary)
                .collect(Collectors.toMap(BookSummary::id, Function.identity()));
    }

    @Override
    public ReservationResult reserve(Long id) {
        try {
            return ReservationResult.ok(toSummary(bookService.reserve(id)));
        } catch (BookNotFoundException e) {
            return ReservationResult.of(Status.NOT_FOUND);
        } catch (BookNotAvailableException e) {
            return ReservationResult.of(Status.NOT_AVAILABLE);
        }
    }

    @Override
    public ReservationResult release(Long id) {
        try {
            return ReservationResult.ok(toSummary(bookService.release(id)));
        } catch (BookNotFoundException e) {
            return ReservationResult.of(Status.NOT_FOUND);
        } catch (BookAlreadyAvailableException e) {
            return ReservationResult.of(Status.ALREADY_AVAILABLE);
        }
    }

    private static BookSummary toSummary(BookDto dto) {
        return new BookSummary(dto.id(), dto.title(), dto.author(), dto.isbn(), dto.available(),
                dto.genre() == null ? null : Genre.valueOf(dto.genre().name()));
    }
}
