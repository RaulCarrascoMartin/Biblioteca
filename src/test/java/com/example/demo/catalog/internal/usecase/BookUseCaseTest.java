package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.model.Genre;
import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookUseCaseTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester USER = new Requester(2L, Set.of("USER"));
    private static final SaveBookDto SAVE = new SaveBookDto("T", "A", "1234567890", Genre.CLASICA);
    private static final BookDto DTO = new BookDto(5L, "T", "A", "1234567890", true, Genre.CLASICA);

    @Mock
    private BookService bookService;

    private BookUseCase useCase() {
        return new BookUseCase(bookService);
    }

    @Test
    void getAll_allowedForAnyAuthenticatedUser() {
        when(bookService.getAll()).thenReturn(List.of(DTO));

        assertEquals(List.of(DTO), useCase().getAll(USER));
    }

    @Test
    void getById_allowedForAnyAuthenticatedUser() {
        when(bookService.getById(5L)).thenReturn(DTO);

        assertEquals(DTO, useCase().getById(USER, 5L));
    }

    @Test
    void create_admin_delegatesAndMaps() {
        when(bookService.create(SAVE)).thenReturn(DTO);

        assertEquals(DTO, useCase().create(ADMIN, SAVE));
    }

    @Test
    void update_admin_delegatesAndMaps() {
        when(bookService.update(5L, SAVE)).thenReturn(DTO);

        assertEquals(DTO, useCase().update(ADMIN, 5L, SAVE));
    }

    @Test
    void delete_admin_delegates() {
        useCase().delete(ADMIN, 5L);

        verify(bookService).delete(5L);
    }

    @Test
    void create_nonAdmin_isForbidden() {
        assertThrows(ForbiddenException.class, () -> useCase().create(USER, SAVE));
        verifyNoInteractions(bookService);
    }

    @Test
    void update_nonAdmin_isForbidden() {
        assertThrows(ForbiddenException.class, () -> useCase().update(USER, 5L, SAVE));
        verifyNoInteractions(bookService);
    }

    @Test
    void delete_nonAdmin_isForbidden() {
        assertThrows(ForbiddenException.class, () -> useCase().delete(USER, 5L));
        verifyNoInteractions(bookService);
    }
}
