package com.example.demo.catalog.internal.application;

import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.dto.ReservationResult;
import com.example.demo.catalog.dto.ReservationResult.Status;
import com.example.demo.catalog.internal.domain.dto.BookDto;
import com.example.demo.catalog.internal.domain.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotFoundException;
import com.example.demo.catalog.internal.domain.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogModuleImplTest {

    private static final BookDto DTO = new BookDto(1L, "T", "A", "1234567890", true,
            com.example.demo.catalog.internal.domain.model.Genre.CLASICA);
    private static final BookSummary SUMMARY = new BookSummary(1L, "T", "A", "1234567890", true, Genre.CLASICA);

    @Mock
    private BookService bookService;

    @InjectMocks
    private CatalogModuleImpl module;

    @Test
    void getById_mapsToPublicDto() {
        when(bookService.findById(1L)).thenReturn(Optional.of(DTO));

        assertEquals(Optional.of(SUMMARY), module.getById(1L));
    }

    @Test
    void getById_missing_isEmpty() {
        when(bookService.findById(1L)).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), module.getById(1L));
    }

    @Test
    void getByIds_mapsToPublicDtos() {
        when(bookService.getByIds(List.of(1L))).thenReturn(Map.of(1L, DTO));

        assertEquals(Map.of(1L, SUMMARY), module.getByIds(List.of(1L)));
    }

    @Test
    void reserve_ok_carriesBook() {
        when(bookService.reserve(1L)).thenReturn(DTO);

        assertEquals(ReservationResult.ok(SUMMARY), module.reserve(1L));
    }

    @Test
    void reserve_notFound() {
        when(bookService.reserve(1L)).thenThrow(new BookNotFoundException());

        ReservationResult result = module.reserve(1L);

        assertEquals(Status.NOT_FOUND, result.status());
        assertNull(result.book());
    }

    @Test
    void reserve_notAvailable() {
        when(bookService.reserve(1L)).thenThrow(new BookNotAvailableException());

        assertEquals(Status.NOT_AVAILABLE, module.reserve(1L).status());
    }

    @Test
    void release_ok_carriesBook() {
        when(bookService.release(1L)).thenReturn(DTO);

        assertEquals(ReservationResult.ok(SUMMARY), module.release(1L));
    }

    @Test
    void release_notFound() {
        when(bookService.release(1L)).thenThrow(new BookNotFoundException());

        assertEquals(Status.NOT_FOUND, module.release(1L).status());
    }

    @Test
    void release_alreadyAvailable() {
        when(bookService.release(1L)).thenThrow(new BookAlreadyAvailableException());

        assertEquals(Status.ALREADY_AVAILABLE, module.release(1L).status());
    }
}
