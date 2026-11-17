package com.example.demo.catalog.internal.controller;

import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.catalog.internal.usecase.BookUseCase;
import com.example.demo.shared.Requester;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Traduce HTTP a llamadas al caso de uso. Sin negocio ni autorización: eso es del caso de uso. */
@RestController
@RequestMapping("/api/libros")
public class BookController implements BookHttpApi {

    private final BookUseCase bookUseCase;

    public BookController(BookUseCase bookUseCase) {
        this.bookUseCase = bookUseCase;
    }

    @Override
    public List<BookResponse> getAll(Requester requester) {
        return bookUseCase.getAll(requester);
    }

    @Override
    public ResponseEntity<BookResponse> getById(Requester requester, @PathVariable Long id) {
        return ResponseEntity.ok(bookUseCase.getById(requester, id));
    }

    @Override
    public ResponseEntity<BookResponse> create(Requester requester, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookUseCase.create(requester, request));
    }

    @Override
    public ResponseEntity<BookResponse> update(Requester requester, @PathVariable Long id,
                                               @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(bookUseCase.update(requester, id, request));
    }

    @Override
    public ResponseEntity<Void> delete(Requester requester, @PathVariable Long id) {
        bookUseCase.delete(requester, id);
        return ResponseEntity.noContent().build();
    }
}
