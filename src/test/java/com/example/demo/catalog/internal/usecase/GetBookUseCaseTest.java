package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.model.Genre;
import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.Requester;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBookUseCaseTest {

    private static final Requester OWNER = new Requester(2L, Set.of("USER"));
    private static final SaveBookDto SAVE = new SaveBookDto("T", "A", "1234567890", Genre.CLASICA);
    private static final BookDto DTO = new BookDto(5L, "T", "A", "1234567890", true, Genre.CLASICA);

    @Mock
    private BookService bookService;

    @InjectMocks
    private GetBookUseCase useCase;

    @Test
    void allowedForAnyAuthenticatedUser() {
        when(bookService.getById(5L)).thenReturn(DTO);

        assertEquals(DTO, useCase.execute(OWNER, 5L));
    }
}
