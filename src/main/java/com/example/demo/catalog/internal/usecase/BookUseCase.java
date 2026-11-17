package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.controller.dto.BookRequest;
import com.example.demo.catalog.internal.controller.dto.BookResponse;
import com.example.demo.catalog.internal.controller.mapper.BookWebMapper;
import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Flujos de usuario sobre el catálogo y su política de acceso: listar y obtener, cualquier autenticado;
 * crear, editar y borrar, solo ADMIN. Mapea el resultado del servicio al DTO web.
 */
@Component
public class BookUseCase {

    private final BookService bookService;
    private final BookWebMapper webMapper;

    public BookUseCase(BookService bookService, BookWebMapper webMapper) {
        this.bookService = bookService;
        this.webMapper = webMapper;
    }

    public List<BookResponse> getAll(Requester requester) {
        return webMapper.toResponses(bookService.getAll());
    }

    public BookResponse getById(Requester requester, Long id) {
        return webMapper.toResponse(bookService.getById(id));
    }

    public BookResponse create(Requester requester, BookRequest request) {
        requireAdmin(requester);
        return webMapper.toResponse(bookService.create(webMapper.toDto(request)));
    }

    public BookResponse update(Requester requester, Long id, BookRequest request) {
        requireAdmin(requester);
        return webMapper.toResponse(bookService.update(id, webMapper.toDto(request)));
    }

    public void delete(Requester requester, Long id) {
        requireAdmin(requester);
        bookService.delete(id);
    }

    private static void requireAdmin(Requester requester) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException();
        }
    }
}
