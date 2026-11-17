package com.example.demo.catalog.internal.usecase;

import com.example.demo.catalog.internal.domain.service.BookService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DeleteBookUseCaseTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester OWNER = new Requester(2L, Set.of("USER"));

    @Mock
    private BookService bookService;

    @InjectMocks
    private DeleteBookUseCase useCase;

    @Test
    void adminDelegatesToTheService() {
        useCase.execute(ADMIN, 5L);

        verify(bookService).delete(5L);
    }

    @Test
    void nonAdminIsForbidden() {
        assertThrows(ForbiddenException.class, () -> useCase.execute(OWNER, 5L));
        verifyNoInteractions(bookService);
    }
}
