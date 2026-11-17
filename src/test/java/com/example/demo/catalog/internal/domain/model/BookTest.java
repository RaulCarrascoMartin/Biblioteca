package com.example.demo.catalog.internal.domain.model;

import com.example.demo.catalog.internal.domain.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.internal.domain.exception.BookNotAvailableException;
import com.example.demo.catalog.internal.domain.exception.InvalidBookException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookTest {

    private static final Isbn ISBN = new Isbn("1234567890");

    private static Book book(boolean available) {
        return new Book(1L, "Título", "Autor", ISBN, available, Genre.CLASICA);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejects_blankTitle(String title) {
        assertThrows(InvalidBookException.class, () -> Book.newBook(title, "Autor", ISBN, Genre.CLASICA));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejects_blankAuthor(String author) {
        assertThrows(InvalidBookException.class, () -> Book.newBook("Título", author, ISBN, Genre.CLASICA));
    }

    @Test
    void rejects_nullIsbn() {
        assertThrows(InvalidBookException.class, () -> Book.newBook("T", "A", null, Genre.CLASICA));
    }

    @Test
    void rejects_nullGenre() {
        assertThrows(InvalidBookException.class, () -> Book.newBook("T", "A", ISBN, null));
    }

    @Test
    void newBook_hasNoIdAndIsAvailable() {
        Book book = Book.newBook("T", "A", ISBN, Genre.POESIA);

        assertNull(book.id());
        assertTrue(book.available());
        assertEquals(Genre.POESIA, book.genre());
    }

    @Test
    void reserve_returnsUnavailableCopy_leavingOriginalUntouched() {
        Book original = book(true);

        Book reserved = original.reserve();

        assertFalse(reserved.available());
        assertTrue(original.available());
        assertEquals(original.id(), reserved.id());
    }

    @Test
    void reserve_whenUnavailable_throws() {
        assertThrows(BookNotAvailableException.class, () -> book(false).reserve());
    }

    @Test
    void release_returnsAvailableCopy() {
        assertTrue(book(false).release().available());
    }

    @Test
    void release_whenAvailable_throws() {
        assertThrows(BookAlreadyAvailableException.class, () -> book(true).release());
    }

    @Test
    void withDetails_replacesFields_keepsIdAndAvailability() {
        Isbn other = new Isbn("0987654321");

        Book updated = book(false).withDetails("T2", "A2", other, Genre.POESIA);

        assertEquals(1L, updated.id());
        assertFalse(updated.available());
        assertEquals("T2", updated.title());
        assertEquals("A2", updated.author());
        assertEquals(other, updated.isbn());
        assertEquals(Genre.POESIA, updated.genre());
    }

    @Test
    void withDetails_nullGenreKeepsCurrent() {
        assertEquals(Genre.CLASICA, book(true).withDetails("T2", "A2", ISBN, null).genre());
    }

    @Test
    void withDetails_revalidatesInvariants() {
        assertThrows(InvalidBookException.class, () -> book(true).withDetails(" ", "A", ISBN, null));
    }
}
