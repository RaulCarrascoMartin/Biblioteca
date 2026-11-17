package com.example.demo.users.internal.usecase;

import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import com.example.demo.users.dto.UserResponseDto;
import com.example.demo.users.internal.controller.dto.UserResponse;
import com.example.demo.users.internal.controller.dto.UserUpdateRequest;
import com.example.demo.users.internal.controller.mapper.UserWebMapper;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    private static final Requester ADMIN = new Requester(1L, Set.of("ADMIN"));
    private static final Requester OWNER = new Requester(2L, Set.of("USER"));
    private static final Requester OTHER = new Requester(3L, Set.of("USER"));

    @Mock
    private UserService service;

    private UserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UserUseCase(service, Mappers.getMapper(UserWebMapper.class));
    }

    private static UserResponseDto dto(Long id) {
        return new UserResponseDto(id, "Ana", "ana@x.es", "USER");
    }

    // ---- getAll: ADMIN

    @Test
    void getAllAllowedForAdmin() {
        when(service.getAll()).thenReturn(List.of(dto(2L)));

        assertEquals(List.of(new UserResponse(2L, "Ana", "ana@x.es", "USER")), useCase.getAll(ADMIN));
    }

    @Test
    void getAllForbiddenForNonAdmin() {
        assertThrows(ForbiddenException.class, () -> useCase.getAll(OWNER));
        verifyNoInteractions(service);
    }

    // ---- me: autenticado

    @Test
    void meUsesRequesterId() {
        when(service.getById(2L)).thenReturn(dto(2L));

        assertEquals(2L, useCase.me(OWNER).id());
    }

    @Test
    void meForbiddenWithoutIdentity() {
        assertThrows(ForbiddenException.class, () -> useCase.me(new Requester(null, Set.of())));
        verifyNoInteractions(service);
    }

    // ---- getById: ADMIN o dueño

    @Test
    void getByIdAllowedForAdmin() {
        when(service.getById(2L)).thenReturn(dto(2L));

        assertEquals("ana@x.es", useCase.getById(ADMIN, 2L).email());
    }

    @Test
    void getByIdAllowedForOwner() {
        when(service.getById(2L)).thenReturn(dto(2L));

        assertEquals(2L, useCase.getById(OWNER, 2L).id());
    }

    @Test
    void getByIdForbiddenForOtherUser() {
        assertThrows(ForbiddenException.class, () -> useCase.getById(OTHER, 2L));
        verifyNoInteractions(service);
    }

    // ---- update: ADMIN o dueño

    @Test
    void updateAllowedForOwnerMapsRequestToDto() {
        when(service.update(2L, new UserUpdateDto("Ana", "ana@x.es", null))).thenReturn(dto(2L));

        UserResponse response = useCase.update(OWNER, 2L, new UserUpdateRequest("Ana", "ana@x.es", null));

        assertEquals("Ana", response.nombre());
        assertEquals("USER", response.rol());
    }

    @Test
    void updateAllowedForAdmin() {
        when(service.update(2L, new UserUpdateDto("Ana", "ana@x.es", null))).thenReturn(dto(2L));

        assertEquals(2L, useCase.update(ADMIN, 2L, new UserUpdateRequest("Ana", "ana@x.es", null)).id());
    }

    @Test
    void updateForbiddenForOtherUser() {
        assertThrows(ForbiddenException.class,
                () -> useCase.update(OTHER, 2L, new UserUpdateRequest("Ana", "ana@x.es", null)));
        verifyNoInteractions(service);
    }

    // ---- revokeSessions: ADMIN o dueño

    @Test
    void revokeSessionsAllowedForAdminAndOwner() {
        useCase.revokeSessions(ADMIN, 2L);
        useCase.revokeSessions(OWNER, 2L);

        org.mockito.Mockito.verify(service, org.mockito.Mockito.times(2)).revokeSessions(2L);
    }

    @Test
    void revokeSessionsForbiddenForOtherUser() {
        assertThrows(ForbiddenException.class, () -> useCase.revokeSessions(OTHER, 2L));
        verifyNoInteractions(service);
    }

    // ---- delete: ADMIN

    @Test
    void deleteAllowedForAdminPassesRequesterId() {
        useCase.delete(ADMIN, 2L);

        verify(service).delete(2L, 1L);
    }

    @Test
    void deleteForbiddenForOwnerAndOthers() {
        assertThrows(ForbiddenException.class, () -> useCase.delete(OWNER, 2L));
        assertThrows(ForbiddenException.class, () -> useCase.delete(OTHER, 2L));
        verifyNoInteractions(service);
    }
}
