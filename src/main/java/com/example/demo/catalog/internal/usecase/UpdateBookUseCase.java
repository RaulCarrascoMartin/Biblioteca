package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

/** Edita un libro. Política: solo ADMIN. */
@Component
public class UpdateBookUseCase {

    private final BookService bookService;

    public UpdateBookUseCase(BookService bookService) {
        this.bookService = bookService;
    }

    public BookDto execute(Requester requester, Long id, SaveBookDto dto) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException();
        }
        return bookService.update(id, dto);
    }
}
