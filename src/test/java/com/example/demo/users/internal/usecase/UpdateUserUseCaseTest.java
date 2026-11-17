package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.service.UserService;
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
class UpdateUserUseCaseTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester OWNER = new Requester(2L, Set.of("USER"));
    private static final Requester OTHER = new Requester(3L, Set.of("USER"));
    private static final UserDto DTO = new UserDto(2L, "Ana", "ana@x.es", "USER");
    private static final UserUpdateDto UPDATE = new UserUpdateDto("Ana", "ana@x.es", null);

    @Mock
    private UserService userService;

    @InjectMocks
    private UpdateUserUseCase useCase;

    @Test
    void allowedForOwner() {
        when(userService.update(2L, UPDATE)).thenReturn(DTO);

        assertEquals("Ana", useCase.execute(OWNER, 2L, UPDATE).name());
    }

    @Test
    void allowedForAdmin() {
        when(userService.update(2L, UPDATE)).thenReturn(DTO);

        assertEquals(2L, useCase.execute(ADMIN, 2L, UPDATE).id());
    }

    @Test
    void forbiddenForAnotherUser() {
        assertThrows(ForbiddenException.class, () -> useCase.execute(OTHER, 2L, UPDATE));
        verifyNoInteractions(userService);
    }
}
