package com.example.demo.catalog.internal.domain.service;

import com.example.demo.catalog.BookInUseChecker;
import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.catalog.dto.Genre;
import com.example.demo.catalog.exception.BookAlreadyAvailableException;
import com.example.demo.catalog.exception.BookNotAvailableException;
import com.example.demo.catalog.exception.BookNotFoundException;
import com.example.demo.catalog.internal.domain.dto.SaveBookDto;
import com.example.demo.catalog.internal.domain.dto.SeedBooksDto;
import com.example.demo.catalog.internal.domain.exception.BookWithLoansException;
import com.example.demo.catalog.internal.domain.exception.DuplicateIsbnException;
import com.example.demo.catalog.internal.domain.mapper.BookMapper;
import com.example.demo.catalog.internal.domain.model.Book;
import com.example.demo.catalog.internal.domain.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository repository;

    private final BookMapper mapper = Mappers.getMapper(BookMapper.class);
    private BookInUseChecker checker;
    private BookServiceImpl service;

    @BeforeEach
    void setUp() {
        checker = mock(BookInUseChecker.class);
        service = new BookServiceImpl(repository, mapper, List.of(checker));
    }

    private static Book book(Long id, String isbn, boolean available) {
        Book b = new Book();
        b.setId(id);
        b.setTitle("Título");
        b.setAuthor("Autor");
        b.setIsbn(isbn);
        b.setAvailable(available);
        b.setGenre(Genre.CLASICA);
        return b;
    }

    private static SaveBookDto dto(String isbn, Genre genre) {
        return new SaveBookDto("Nuevo", "Autora", isbn, genre);
    }

    @Test
    void getAll_mapsModelsToDtos() {
        when(repository.findAll()).thenReturn(List.of(book(1L, "111", true)));

        List<BookResponseDto> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("111", result.get(0).isbn());
    }

    @Test
    void findById_missing_returnsEmpty() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertTrue(service.findById(9L).isEmpty());
    }

    @Test
    void getById_missing_throwsNotFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> service.getById(9L));
    }

    @Test
    void getById_existing_returnsDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(book(1L, "111", true)));

        assertEquals(1L, service.getById(1L).id());
    }

    @Test
    void getByIds_indexesById_andOmitsMissing() {
        when(repository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(book(1L, "111", true), book(2L, "222", false)));

        Map<Long, BookResponseDto> result = service.getByIds(List.of(1L, 2L));

        assertEquals(2, result.size());
        assertFalse(result.get(2L).available());
    }

    @Test
    void create_newIsbn_savesAvailableBook() {
        when(repository.findByIsbn("111")).thenReturn(Optional.empty());
        when(repository.save(any(Book.class))).thenAnswer(inv -> {
            Book saved = inv.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        BookResponseDto result = service.create(dto("111", Genre.MISTERIO));

        assertEquals(5L, result.id());
        assertTrue(result.available());
        assertEquals(Genre.MISTERIO, result.genre());
    }

    @Test
    void create_duplicateIsbn_throwsAndDoesNotSave() {
        when(repository.findByIsbn("111")).thenReturn(Optional.of(book(1L, "111", true)));

        assertThrows(DuplicateIsbnException.class, () -> service.create(dto("111", null)));
        verify(repository, never()).save(any());
    }

    @Test
    void update_missing_throwsNotFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> service.update(9L, dto("111", null)));
    }

    @Test
    void update_isbnOfAnotherBook_throwsDuplicate() {
        when(repository.findById(1L)).thenReturn(Optional.of(book(1L, "111", true)));
        when(repository.findByIsbn("222")).thenReturn(Optional.of(book(2L, "222", true)));

        assertThrows(DuplicateIsbnException.class, () -> service.update(1L, dto("222", null)));
        verify(repository, never()).save(any());
    }

    @Test
    void update_sameIsbn_updatesFields_keepsAvailabilityAndGenreWhenNull() {
        Book existing = book(1L, "111", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByIsbn("111")).thenReturn(Optional.of(existing));
        when(repository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookResponseDto result = service.update(1L, dto("111", null));

        assertEquals("Nuevo", result.title());
        assertFalse(result.available());
        assertEquals(Genre.CLASICA, result.genre());
    }

    @Test
    void reserve_availableBook_marksUnavailable() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(book(1L, "111", true)));
        when(repository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        assertFalse(service.reserve(1L).available());
    }

    @Test
    void reserve_unavailableBook_throws() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(book(1L, "111", false)));

        assertThrows(BookNotAvailableException.class, () -> service.reserve(1L));
        verify(repository, never()).save(any());
    }

    @Test
    void reserve_missing_throwsNotFound() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> service.reserve(1L));
    }

    @Test
    void release_unavailableBook_marksAvailable() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(book(1L, "111", false)));
        when(repository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(service.release(1L).available());
    }

    @Test
    void release_availableBook_throws() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.of(book(1L, "111", true)));

        assertThrows(BookAlreadyAvailableException.class, () -> service.release(1L));
    }

    @Test
    void release_missing_throwsNotFound() {
        when(repository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> service.release(1L));
    }

    @Test
    void delete_notInUse_deletes() {
        when(repository.findById(1L)).thenReturn(Optional.of(book(1L, "111", true)));
        when(checker.isInUse(1L)).thenReturn(false);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_vetoedByChecker_throwsAndDoesNotDelete() {
        when(repository.findById(1L)).thenReturn(Optional.of(book(1L, "111", true)));
        when(checker.isInUse(1L)).thenReturn(true);

        assertThrows(BookWithLoansException.class, () -> service.delete(1L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void delete_missing_throwsNotFound_withoutAskingCheckers() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> service.delete(9L));
        verify(checker, never()).isInUse(any());
        verify(repository, never()).deleteById(any());
    }

    @Test
    void delete_withoutCheckers_deletes() {
        BookServiceImpl noCheckers = new BookServiceImpl(repository, mapper, List.of());
        when(repository.findById(1L)).thenReturn(Optional.of(book(1L, "111", true)));

        noCheckers.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void seed_createsMissing_updatesGenre_ignoresEqual() {
        when(repository.findByIsbn("new")).thenReturn(Optional.empty());
        when(repository.findByIsbn("diff")).thenReturn(Optional.of(book(1L, "diff", true)));
        when(repository.findByIsbn("same")).thenReturn(Optional.of(book(2L, "same", true)));

        SeedBooksDto result = service.seed(List.of(
                dto("new", Genre.POESIA), dto("diff", Genre.POESIA), dto("same", Genre.CLASICA)));

        assertEquals(new SeedBooksDto(1, 1), result);
        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(repository, org.mockito.Mockito.times(2)).save(saved.capture());
        assertTrue(saved.getAllValues().stream().allMatch(b -> b.getGenre() == Genre.POESIA));
    }
}
