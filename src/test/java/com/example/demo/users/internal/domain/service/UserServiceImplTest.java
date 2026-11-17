package com.example.demo.users.internal.domain.service;

import com.example.demo.shared.PasswordHasher;
import com.example.demo.users.internal.domain.dto.NewUserDto;
import com.example.demo.users.internal.domain.dto.UserDto;
import com.example.demo.users.internal.domain.dto.UserUpdateDto;
import com.example.demo.users.internal.domain.exception.EmailAlreadyRegisteredException;
import com.example.demo.users.internal.domain.exception.InvalidEmailException;
import com.example.demo.users.internal.domain.exception.SelfDeletionException;
import com.example.demo.users.internal.domain.exception.UserNotFoundException;
import com.example.demo.users.internal.domain.exception.UserWithLoansException;
import com.example.demo.users.internal.domain.mapper.UserMapper;
import com.example.demo.users.internal.domain.model.Email;
import com.example.demo.users.internal.domain.model.Role;
import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.domain.repository.UserRepository;
import com.example.demo.users.spi.UserInUseChecker;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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
        return new User(id, name, new Email(email), "hash", Role.USER, 0);
    }

    @Test
    void getAllMapsEveryUser() {
        when(repository.findAll()).thenReturn(List.of(user(1L, "Ana", "ana@x.es"), user(2L, "Bob", "bob@x.es")));

        List<UserDto> result = service.getAll();

        assertEquals(2, result.size());
        assertEquals("Ana", result.get(0).name());
    }

    @Test
    void getByIdReturnsDtoWithoutPassword() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "Ana", "ana@x.es")));

        UserDto dto = service.getById(1L);

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

        Map<Long, UserDto> result = service.getByIds(List.of(1L, 2L));

        assertEquals("Ana", result.get(1L).name());
        assertEquals("Bob", result.get(2L).name());
    }

    @Test
    void registerHashesPasswordAndSavesNormalizedUserRole() {
        when(repository.findByEmail("ana@x.es")).thenReturn(Optional.empty());
        when(hasher.hash("Secreta123")).thenReturn("HASH");
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new User(7L, u.name(), u.email(), u.passwordHash(), u.role(), u.tokenVersion());
        });

        Optional<UserDto> result = service.register(new NewUserDto("  Ana ", "  ANA@x.es ", "Secreta123"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertNull(saved.getValue().id());
        assertEquals("Ana", saved.getValue().name());
        assertEquals("ana@x.es", saved.getValue().email().value());
        assertEquals("HASH", saved.getValue().passwordHash());
        assertEquals(Role.USER, saved.getValue().role());
        assertEquals(7L, result.orElseThrow().id());
    }

    @Test
    void registerReturnsEmptyWhenEmailExists() {
        when(repository.findByEmail("ana@x.es")).thenReturn(Optional.of(user(1L, "Ana", "ana@x.es")));

        assertTrue(service.register(new NewUserDto("Ana", "ana@x.es", "Secreta123")).isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    void registerRejectsInvalidEmail() {
        assertThrows(InvalidEmailException.class, () -> service.register(new NewUserDto("Ana", "mal", "Secreta123")));
        verify(repository, never()).save(any());
    }

    @Test
    void findCredentialsExposesHashAndTokenVersion() {
        User u = new User(1L, "Ana", new Email("ana@x.es"), "hash", Role.USER, 3);
        when(repository.findByEmail("ana@x.es")).thenReturn(Optional.of(u));

        var credentials = service.findCredentialsByEmail("ana@x.es").orElseThrow();

        assertEquals("hash", credentials.passwordHash());
        assertEquals(3, credentials.tokenVersion());
        assertFalse(credentials.toString().contains("hash"));
    }

    @Test
    void updateChangesNameAndEmailAndKeepsPasswordWhenBlank() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "Ana", "ana@x.es")));
        when(repository.findByEmail("ana2@x.es")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto dto = service.update(1L, new UserUpdateDto(" Ana M ", "ANA2@x.es", " "));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertEquals("Ana M", dto.name());
        assertEquals("ana2@x.es", dto.email());
        assertEquals("hash", saved.getValue().passwordHash());
        assertEquals(0, saved.getValue().tokenVersion());
    }

    @Test
    void updateWithPasswordHashesItAndBumpsTokenVersion() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "Ana", "ana@x.es")));
        when(hasher.hash("Nueva4567")).thenReturn("NEWHASH");
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        service.update(1L, new UserUpdateDto("Ana", "ANA@x.es", "Nueva4567"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertEquals("NEWHASH", saved.getValue().passwordHash());
        assertEquals(1, saved.getValue().tokenVersion());
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
    void revokeSessionsSavesUserWithBumpedTokenVersion() {
        when(repository.findById(1L)).thenReturn(Optional.of(user(1L, "Ana", "ana@x.es")));

        service.revokeSessions(1L);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertEquals(1, saved.getValue().tokenVersion());
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

    @Test
    void ensureAdminCreatesAdminWithHashedPassword() {
        when(repository.findByEmail("admin@x.es")).thenReturn(Optional.empty());
        when(hasher.hash("Secreta123")).thenReturn("HASH");

        service.ensureAdmin(" Admin@X.es ", "Secreta123");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertNull(saved.getValue().id());
        assertEquals(Role.ADMIN, saved.getValue().role());
        assertEquals("admin@x.es", saved.getValue().email().value());
        assertEquals("HASH", saved.getValue().passwordHash());
    }

    @Test
    void ensureAdminDoesNothingWhenEmailAlreadyExists() {
        when(repository.findByEmail("admin@x.es")).thenReturn(Optional.of(user(1L, "Admin", "admin@x.es")));

        service.ensureAdmin("admin@x.es", "Secreta123");

        verify(repository, never()).save(any());
    }

    @Test
    void ensureAdminRejectsInvalidEmail() {
        assertThrows(InvalidEmailException.class, () -> service.ensureAdmin("mal", "Secreta123"));
        verify(repository, never()).save(any());
    }
}
