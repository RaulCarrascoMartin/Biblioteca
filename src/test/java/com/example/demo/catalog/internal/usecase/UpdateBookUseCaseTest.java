package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.model.Genre;
import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBookUseCaseTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester OWNER = new Requester(2L, Set.of("USER"));
    private static final SaveBookDto SAVE = new SaveBookDto("T", "A", "1234567890", Genre.CLASICA);
    private static final BookDto DTO = new BookDto(5L, "T", "A", "1234567890", true, Genre.CLASICA);

    @Mock
    private BookService bookService;

    @InjectMocks
    private UpdateBookUseCase useCase;

    @Test
    void adminDelegatesToTheService() {
        when(bookService.update(5L, SAVE)).thenReturn(DTO);

        assertEquals(DTO, useCase.execute(ADMIN, 5L, SAVE));
    }

    @Test
    void nonAdminIsForbidden() {
        assertThrows(ForbiddenException.class, () -> useCase.execute(OWNER, 5L, SAVE));
        verifyNoInteractions(bookService);
    }
}
