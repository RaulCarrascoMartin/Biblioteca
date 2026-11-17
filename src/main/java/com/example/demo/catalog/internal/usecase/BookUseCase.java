package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Flujos de usuario sobre el catálogo y su política de acceso: listar y obtener, cualquier autenticado;
 * crear, editar y borrar, solo ADMIN. Trabaja con DTO de dominio; el mapeo web es del controller.
 */
@Component
public class BookUseCase {

    private final BookService bookService;

    public BookUseCase(BookService bookService) {
        this.bookService = bookService;
    }

    public List<BookDto> getAll(Requester requester) {
        return bookService.getAll();
    }

    public BookDto getById(Requester requester, Long id) {
        return bookService.getById(id);
    }

    public BookDto create(Requester requester, SaveBookDto dto) {
        requireAdmin(requester);
        return bookService.create(dto);
    }

    public BookDto update(Requester requester, Long id, SaveBookDto dto) {
        requireAdmin(requester);
        return bookService.update(id, dto);
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
