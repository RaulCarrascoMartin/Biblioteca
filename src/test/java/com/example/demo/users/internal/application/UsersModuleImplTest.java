package com.example.demo.users.internal.application;

import com.example.demo.users.dto.UserCredentialsDto;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import com.example.demo.users.internal.domain.service.UserService;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersModuleImplTest {

    @Mock
    private UserService service;
    @InjectMocks
    private UsersModuleImpl module;

    private final UserResponseDto ana = new UserResponseDto(1L, "Ana", "ana@x.es", "USER");

    @Test
    void getByIdDelegatesToOptionalSearch() {
        when(service.findById(1L)).thenReturn(Optional.of(ana));
        when(service.findById(2L)).thenReturn(Optional.empty());

        assertSame(ana, module.getById(1L).orElseThrow());
        assertEquals(Optional.empty(), module.getById(2L));
    }

    @Test
    void getByEmailDelegates() {
        when(service.findByEmail("ana@x.es")).thenReturn(Optional.of(ana));

        assertSame(ana, module.getByEmail("ana@x.es").orElseThrow());
    }

    @Test
    void getByIdsDelegates() {
        when(service.getByIds(List.of(1L))).thenReturn(Map.of(1L, ana));

        assertEquals(Map.of(1L, ana), module.getByIds(List.of(1L)));
    }

    @Test
    void registerDelegates() {
        UserDto dto = new UserDto("Ana", "ana@x.es", "Secreta123");
        when(service.register(dto)).thenReturn(Optional.of(ana));

        assertSame(ana, module.register(dto).orElseThrow());
    }

    @Test
    void getCredentialsDelegates() {
        UserCredentialsDto credentials = new UserCredentialsDto("ana@x.es", "hash", "USER", 0);
        when(service.findCredentialsByEmail("ana@x.es")).thenReturn(Optional.of(credentials));

        assertSame(credentials, module.getCredentials("ana@x.es").orElseThrow());
    }
}
