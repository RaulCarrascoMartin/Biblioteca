package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.service.UserService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RevokeUserSessionsUseCaseTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester OWNER = new Requester(2L, Set.of("USER"));
    private static final Requester OTHER = new Requester(3L, Set.of("USER"));

    @Mock
    private UserService userService;

    @InjectMocks
    private RevokeUserSessionsUseCase useCase;

    @Test
    void allowedForAdminAndOwner() {
        useCase.execute(ADMIN, 2L);
        useCase.execute(OWNER, 2L);

        verify(userService, times(2)).revokeSessions(2L);
    }

    @Test
    void forbiddenForAnotherUser() {
        assertThrows(ForbiddenException.class, () -> useCase.execute(OTHER, 2L));
        verifyNoInteractions(userService);
    }
}
