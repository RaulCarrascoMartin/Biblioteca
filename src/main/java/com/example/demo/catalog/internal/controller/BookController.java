package com.example.demo.catalog.internal.controller;

import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.catalog.internal.controller.mapper.BookWebMapper;
import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.usecase.CreateBookUseCase;
import com.example.demo.catalog.internal.usecase.DeleteBookUseCase;
import com.example.demo.catalog.internal.usecase.GetAllBooksUseCase;
import com.example.demo.catalog.internal.usecase.GetBookUseCase;
import com.example.demo.catalog.internal.usecase.UpdateBookUseCase;
import com.example.demo.shared.Requester;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/** Traduce HTTP a llamadas al caso de uso y mapea DTO web ↔ DTO de dominio. Sin negocio ni autorización. */
@RestController
public class BookController implements BookHttpApi {

    private final GetAllBooksUseCase getAllBooksUseCase;
    private final GetBookUseCase getBookUseCase;
    private final CreateBookUseCase createBookUseCase;
    private final UpdateBookUseCase updateBookUseCase;
    private final DeleteBookUseCase deleteBookUseCase;
    private final BookWebMapper mapper;

    public BookController(GetAllBooksUseCase getAllBooksUseCase,
                          GetBookUseCase getBookUseCase,
                          CreateBookUseCase createBookUseCase,
                          UpdateBookUseCase updateBookUseCase,
                          DeleteBookUseCase deleteBookUseCase,
                          BookWebMapper mapper) {
        this.getAllBooksUseCase = getAllBooksUseCase;
        this.getBookUseCase = getBookUseCase;
        this.createBookUseCase = createBookUseCase;
        this.updateBookUseCase = updateBookUseCase;
        this.deleteBookUseCase = deleteBookUseCase;
        this.mapper = mapper;
    }

    @Override
    public List<BookResponse> getAll(Requester requester) {
        return mapper.toResponses(getAllBooksUseCase.execute(requester));
    }

    @Override
    public ResponseEntity<BookResponse> getById(Requester requester, Long id) {
        return ResponseEntity.ok(mapper.toResponse(getBookUseCase.execute(requester, id)));
    }

    @Override
    public ResponseEntity<BookResponse> create(Requester requester, BookRequest request) {
        BookDto created = createBookUseCase.execute(requester, mapper.toDto(request));
        return ResponseEntity.created(URI.create(BASE_PATH + "/" + created.id()))
                .body(mapper.toResponse(created));
    }

    @Override
    public ResponseEntity<BookResponse> update(Requester requester, Long id, BookRequest request) {
        return ResponseEntity.ok(mapper.toResponse(updateBookUseCase.execute(requester, id, mapper.toDto(request))));
    }

    @Override
    public ResponseEntity<Void> delete(Requester requester, Long id) {
        deleteBookUseCase.execute(requester, id);
        return ResponseEntity.noContent().build();
    }
}
