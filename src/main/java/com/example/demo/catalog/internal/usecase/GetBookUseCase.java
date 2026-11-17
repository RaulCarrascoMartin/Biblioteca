package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

/** Obtiene un libro. Política: cualquier usuario autenticado. */
@Component
public class GetBookUseCase {

    private final BookService bookService;

    public GetBookUseCase(BookService bookService) {
        this.bookService = bookService;
    }

    public BookDto execute(Requester requester, Long id) {
        return bookService.getById(id);
    }
}
