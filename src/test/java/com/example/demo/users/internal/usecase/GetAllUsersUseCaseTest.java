package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.service.UserService;
import java.util.List;
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
class GetAllUsersUseCaseTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester OWNER = new Requester(2L, Set.of("USER"));
    private static final UserDto DTO = new UserDto(2L, "Ana", "ana@x.es", "USER");
    private static final UserUpdateDto UPDATE = new UserUpdateDto("Ana", "ana@x.es", null);

    @Mock
    private UserService userService;

    @InjectMocks
    private GetAllUsersUseCase useCase;

    @Test
    void allowedForAdmin() {
        when(userService.getAll()).thenReturn(List.of(DTO));

        assertEquals(List.of(DTO), useCase.execute(ADMIN));
    }

    @Test
    void forbiddenForNonAdmin() {
        assertThrows(ForbiddenException.class, () -> useCase.execute(OWNER));
        verifyNoInteractions(userService);
    }
}
