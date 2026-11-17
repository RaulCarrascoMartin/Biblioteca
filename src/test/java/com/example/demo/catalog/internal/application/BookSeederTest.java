package com.example.demo.catalog.internal.application;

import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.dto.SeedBooksDto;
import com.example.demo.catalog.internal.domain.service.BookService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookSeederTest {

    @Test
    void catalog_hasNoDuplicateIsbnAndEveryBookHasGenre() {
        List<SaveBookDto> catalog = BookSeeder.catalog();

        Set<String> isbns = catalog.stream().map(SaveBookDto::isbn).collect(Collectors.toSet());
        assertFalse(catalog.isEmpty());
        assertEquals(catalog.size(), isbns.size());
        assertEquals(catalog.size(), catalog.stream().filter(b -> b.genre() != null).count());
    }

    @Test
    void run_sendsTheCatalogToTheService() {
        BookService service = mock(BookService.class);
        when(service.seed(BookSeeder.catalog())).thenReturn(new SeedBooksDto(2, 1));

        new BookSeeder(service).run();

        verify(service).seed(BookSeeder.catalog());
    }
}
