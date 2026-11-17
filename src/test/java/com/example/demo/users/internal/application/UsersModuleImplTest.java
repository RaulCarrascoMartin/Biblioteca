package com.example.demo.users.internal.application;

import com.example.demo.users.dto.NewUser;
import com.example.demo.users.dto.UserCredentials;
import com.example.demo.users.dto.UserSummary;
import com.example.demo.users.internal.domain.dto.NewUserDto;
import com.example.demo.users.internal.domain.dto.UserCredentialsDto;
import com.example.demo.users.internal.domain.dto.UserDto;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersModuleImplTest {

    @Mock
    private UserService service;
    @InjectMocks
    private UsersModuleImpl module;

    private final UserDto ana = new UserDto(1L, "Ana", "ana@x.es", "USER");
    private final UserSummary anaSummary = new UserSummary(1L, "Ana", "ana@x.es", "USER");

    @Test
    void getByIdMapsToPublicDto() {
        when(service.findById(1L)).thenReturn(Optional.of(ana));
        when(service.findById(2L)).thenReturn(Optional.empty());

        assertEquals(anaSummary, module.getById(1L).orElseThrow());
        assertEquals(Optional.empty(), module.getById(2L));
    }

    @Test
    void getByEmailMapsToPublicDto() {
        when(service.findByEmail("ana@x.es")).thenReturn(Optional.of(ana));

        assertEquals(anaSummary, module.getByEmail("ana@x.es").orElseThrow());
    }

    @Test
    void getByIdsMapsEveryValue() {
        when(service.getByIds(List.of(1L))).thenReturn(Map.of(1L, ana));

        assertEquals(Map.of(1L, anaSummary), module.getByIds(List.of(1L)));
    }

    @Test
    void registerMapsInputAndOutput() {
        when(service.register(new NewUserDto("Ana", "ana@x.es", "Secreta123"))).thenReturn(Optional.of(ana));

        assertEquals(anaSummary, module.register(new NewUser("Ana", "ana@x.es", "Secreta123")).orElseThrow());
    }

    @Test
    void registerEmptyStaysEmpty() {
        when(service.register(new NewUserDto("Ana", "ana@x.es", "x"))).thenReturn(Optional.empty());

        assertEquals(Optional.empty(), module.register(new NewUser("Ana", "ana@x.es", "x")));
    }

    @Test
    void getCredentialsMapsHashToPassword() {
        when(service.findCredentialsByEmail("ana@x.es"))
                .thenReturn(Optional.of(new UserCredentialsDto("ana@x.es", "hash", "USER", 3)));

        assertEquals(new UserCredentials("ana@x.es", "hash", "USER", 3),
                module.getCredentials("ana@x.es").orElseThrow());
    }

    @Test
    void ensureAdminDelegates() {
        module.ensureAdmin("admin@x.es", "Secreta123");

        verify(service).ensureAdmin("admin@x.es", "Secreta123");
    }
}
