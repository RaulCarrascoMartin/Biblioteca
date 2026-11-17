package com.example.demo.catalog.internal.controller;

import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.catalog.internal.controller.mapper.BookWebMapper;
import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.usecase.BookUseCase;
import com.example.demo.shared.Requester;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** Traduce HTTP a llamadas al caso de uso y mapea DTO web ↔ DTO de dominio. Sin negocio ni autorización. */
@RestController
public class BookController implements BookHttpApi {

    private final BookUseCase bookUseCase;
    private final BookWebMapper mapper;

    public BookController(BookUseCase bookUseCase, BookWebMapper mapper) {
        this.bookUseCase = bookUseCase;
        this.mapper = mapper;
    }

    @Override
    public List<BookResponse> getAll(Requester requester) {
        return mapper.toResponses(bookUseCase.getAll(requester));
    }

    @Override
    public ResponseEntity<BookResponse> getById(Requester requester, Long id) {
        return ResponseEntity.ok(mapper.toResponse(bookUseCase.getById(requester, id)));
    }

    @Override
    public ResponseEntity<BookResponse> create(Requester requester, BookRequest request) {
        BookDto created = bookUseCase.create(requester, mapper.toDto(request));
        return ResponseEntity.created(URI.create(BASE_PATH + "/" + created.id()))
                .body(mapper.toResponse(created));
    }

    @Override
    public ResponseEntity<BookResponse> update(Requester requester, Long id, BookRequest request) {
        return ResponseEntity.ok(mapper.toResponse(bookUseCase.update(requester, id, mapper.toDto(request))));
    }

    @Override
    public ResponseEntity<Void> delete(Requester requester, Long id) {
        bookUseCase.delete(requester, id);
        return ResponseEntity.noContent().build();
    }
}
