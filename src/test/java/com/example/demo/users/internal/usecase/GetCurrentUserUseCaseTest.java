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
class GetCurrentUserUseCaseTest {

    private static final Requester OWNER = new Requester(2L, Set.of("USER"));
    private static final Requester ANONYMOUS = new Requester(null, Set.of());
    private static final UserDto DTO = new UserDto(2L, "Ana", "ana@x.es", "USER");
    private static final UserUpdateDto UPDATE = new UserUpdateDto("Ana", "ana@x.es", null);

    @Mock
    private UserService userService;

    @InjectMocks
    private GetCurrentUserUseCase useCase;

    @Test
    void usesTheRequesterId() {
        when(userService.getById(2L)).thenReturn(DTO);

        assertEquals(2L, useCase.execute(OWNER).id());
    }

    @Test
    void forbiddenWithoutIdentity() {
        assertThrows(ForbiddenException.class, () -> useCase.execute(ANONYMOUS));
        verifyNoInteractions(userService);
    }
}
