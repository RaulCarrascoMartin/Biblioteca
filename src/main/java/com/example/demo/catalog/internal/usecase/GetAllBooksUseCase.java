package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.Requester;
import java.util.List;
import org.springframework.stereotype.Component;

/** Lista el catálogo. Política: cualquier usuario autenticado. */
@Component
public class GetAllBooksUseCase {

    private final BookService bookService;

    public GetAllBooksUseCase(BookService bookService) {
        this.bookService = bookService;
    }

    public List<BookDto> execute(Requester requester) {
        return bookService.getAll();
    }
}
