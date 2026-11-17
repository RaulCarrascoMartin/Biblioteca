package com.example.demo.users.internal.domain.service;

import com.example.demo.shared.PasswordHasher;
import com.example.demo.users.UserInUseChecker;
import com.example.demo.users.dto.UserDto;
import com.example.demo.users.dto.UserResponseDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException;
import com.example.demo.users.internal.domain.exception.SelfDeletionException;
import com.example.demo.users.internal.domain.exception.UserNotFoundException;
import com.example.demo.users.internal.domain.exception.UserWithLoansException;
import com.example.demo.users.internal.domain.mapper.UserMapper;
import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.domain.repository.UserRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository repository;
    @Mock
    private PasswordHasher hasher;
    @Mock
    private UserInUseChecker checker;

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(repository, Mappers.getMapper(UserMapper.class), List.of(checker), hasher);
    }

    private static User user(Long id, String name, String email) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPassword("hash");
        return u;
    }

    @Test
    void getAllMapsEveryUser() {
        when(repository.findAll()).thenReturn(List.of(user(1L, "Ana", "ana@x.es"), user(2L, "Bob", "bob@x.es")));

        List<UserResponseDto> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals("Ana", result.get(0).name());
    }

    @Test
    void getByIdReturnsDtoWithoutPassword() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "Ana", "ana@x.es")));

        UserResponseDto dto = service.getById(1L);

        assertEquals("ana@x.es", dto.email());
        assertEquals("USER", dto.role());
    }

    @Test
    void getByIdThrowsWhenMissing() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> service.getById(9L));
        assertTrue(ex.getMessage().contains("9"));
    }

    @Test
    void findByIdAndFindByEmailAreOptional() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        when(repository.findByEmail("ana@x.es")).thenReturn(Optional.of(user(1L, "Ana", "ana@x.es")));

        assertTrue(service.findById(9L).isEmpty());
        assertEquals(1L, service.findByEmail("ana@x.es").orElseThrow().id());
    }

    @Test
    void getByIdsIndexesById() {
        when(repository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(user(1L, "Ana", "a@x.es"), user(2L, "Bob", "b@x.es")));

        Map<Long, UserResponseDto> result = service.getByIds(List.of(1L, 2L));

        assertEquals(Map.of(1L, "Ana", 2L, "Bob"),
                Map.of(1L, result.get(1L).name(), 2L, result.get(2L).name()));
    }

    @Test
    void registerNormalizesEmailHashesPasswordAndForcesUserRole() {
        when(repository.findByEmail("ana@x.es")).thenReturn(Optional.empty());
        when(hasher.hash("Secreta123")).thenReturn("HASH");
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(7L);
            return u;
        });

        Optional<UserResponseDto> result = service.register(new UserDto("  Ana ", "  ANA@x.es ", "Secreta123"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertEquals("Ana", saved.getValue().getName());
        assertEquals("ana@x.es", saved.getValue().getEmail());
        assertEquals("HASH", saved.getValue().getPassword());
        assertEquals("USER", saved.getValue().getRole());
        assertEquals(7L, result.orElseThrow().id());
    }

    @Test
    void registerReturnsEmptyWhenEmailExists() {
        when(repository.findByEmail("ana@x.es")).thenReturn(Optional.of(user(1L, "Ana", "ana@x.es")));

        assertTrue(service.register(new UserDto("Ana", "ana@x.es", "Secreta123")).isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void findCredentialsExposesHashAndTokenVersion() {
        User u = user(1L, "Ana", "ana@x.es");
        u.setTokenVersion(3);
        when(repository.findByEmail("ana@x.es")).thenReturn(Optional.of(u));

        var credentials = service.findCredentialsByEmail("ana@x.es").orElseThrow();

        assertEquals("hash", credentials.password());
        assertEquals(3, credentials.tokenVersion());
        assertFalse(credentials.toString().contains("hash"));
    }

    @Test
    void updateChangesNameAndEmailAndKeepsPasswordWhenBlank() {
        User existing = user(1L, "Ana", "ana@x.es");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("ana2@x.es")).thenReturn(Optional.empty());
        when(repository.save(existing)).thenReturn(existing);

        UserResponseDto dto = service.update(1L, new UserUpdateDto(" Ana M ", "ANA2@x.es", " "));

        assertEquals("Ana M", dto.name());
        assertEquals("ana2@x.es", dto.email());
        assertEquals("hash", existing.getPassword());
        assertEquals(0, existing.getTokenVersion());
    }

    @Test
    void updateWithPasswordHashesItAndBumpsTokenVersion() {
        User existing = user(1L, "Ana", "ana@x.es");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(hasher.hash("Nueva4567")).thenReturn("NEWHASH");
        when(repository.save(existing)).thenReturn(existing);

        service.update(1L, new UserUpdateDto("Ana", "ana@x.es", "Nueva4567"));

        assertEquals("NEWHASH", existing.getPassword());
        assertEquals(1, existing.getTokenVersion());
        verify(repository, never()).findByEmail(any());
    }

    @Test
    void updateRejectsEmailOfAnotherUser() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "Ana", "ana@x.es")));
        when(repository.findByEmail("bob@x.es")).thenReturn(Optional.of(user(2L, "Bob", "bob@x.es")));

        assertThrows(EmailAlreadyRegisteredException.class,
                () -> service.update(1L, new UserUpdateDto("Ana", "bob@x.es", null)));
        verify(repository, never()).save(any());
    }

    @Test
    void updateThrowsWhenMissing() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.update(9L, new UserUpdateDto("A", "a@x.es", null)));
    }

    @Test
    void revokeSessionsBumpsTokenVersion() {
        User existing = user(1L, "Ana", "ana@x.es");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.revokeSessions(1L);

        assertEquals(1, existing.getTokenVersion());
        verify(repository).save(existing);
    }

    @Test
    void revokeSessionsThrowsWhenMissing() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.revokeSessions(9L));
    }

    @Test
    void deleteRemovesUserWhenNobodyUsesIt() {
        when(repository.findById(2L)).thenReturn(Optional.of(user(2L, "Bob", "b@x.es")));
        when(checker.isInUse(2L)).thenReturn(false);

        service.delete(2L, 1L);

        verify(repository).deleteById(2L);
    }

    @Test
    void deleteRejectsSelfDeletion() {
        assertThrows(SelfDeletionException.class, () -> service.delete(1L, 1L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.delete(2L, 1L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void deleteIsVetoedWhenAnyCheckerSaysInUse() {
        when(repository.findById(2L)).thenReturn(Optional.of(user(2L, "Bob", "b@x.es")));
        when(checker.isInUse(2L)).thenReturn(true);

        assertThrows(UserWithLoansException.class, () -> service.delete(2L, 1L));
        verify(repository, never()).deleteById(any());
    }
}
