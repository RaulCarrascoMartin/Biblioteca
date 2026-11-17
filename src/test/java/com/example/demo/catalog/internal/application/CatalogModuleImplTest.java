package com.example.demo.catalog.internal.application;

import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.exception.BookNotAvailableException;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogModuleImplTest {

    private static final BookResponseDto DTO = new BookResponseDto(1L, "T", "A", "123", true, Genre.CLASICA);

    @Mock
    private BookService bookService;

    @InjectMocks
    private CatalogModuleImpl module;

    @Test
    void getById_delegatesToFindById() {
        when(bookService.findById(1L)).thenReturn(Optional.of(DTO));

        assertEquals(Optional.of(DTO), module.getById(1L));
    }

    @Test
    void getByIds_delegates() {
        when(bookService.getByIds(List.of(1L))).thenReturn(Map.of(1L, DTO));

        assertEquals(Map.of(1L, DTO), module.getByIds(List.of(1L)));
    }

    @Test
    void reserve_delegatesAndReturnsServiceDto() {
        when(bookService.reserve(1L)).thenReturn(DTO);

        assertSame(DTO, module.reserve(1L));
    }

    @Test
    void reserve_propagatesPublicException() {
        when(bookService.reserve(1L)).thenThrow(new BookNotAvailableException());

        assertThrows(BookNotAvailableException.class, () -> module.reserve(1L));
    }

    @Test
    void release_delegates() {
        when(bookService.release(1L)).thenReturn(DTO);

        assertSame(DTO, module.release(1L));
        verify(bookService).release(1L);
    }
}
