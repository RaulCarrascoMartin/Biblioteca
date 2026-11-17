package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

/** Crea un libro. Política: solo ADMIN. */
@Component
public class CreateBookUseCase {

    private final BookService bookService;

    public CreateBookUseCase(BookService bookService) {
        this.bookService = bookService;
    }

    public BookDto execute(Requester requester, SaveBookDto dto) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException();
        }
        return bookService.create(dto);
    }
}
