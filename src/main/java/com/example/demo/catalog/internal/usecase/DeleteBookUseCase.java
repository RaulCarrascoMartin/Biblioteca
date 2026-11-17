package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

/** Retira un libro. Política: solo ADMIN. */
@Component
public class DeleteBookUseCase {

    private final BookService bookService;

    public DeleteBookUseCase(BookService bookService) {
        this.bookService = bookService;
    }

    public void execute(Requester requester, Long id) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException();
        }
        bookService.delete(id);
    }
}
