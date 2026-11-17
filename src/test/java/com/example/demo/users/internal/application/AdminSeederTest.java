package com.example.demo.users.internal.application;

import com.example.demo.shared.PasswordHasher;
import com.example.demo.users.internal.domain.model.User;
import com.example.demo.users.internal.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UserRepository repository;
    @Mock
    private PasswordHasher hasher;

    private AdminSeeder seeder(String email, String password) {
        return new AdminSeeder(repository, hasher, email, password);
    }

    @Test
    void doesNothingWithoutConfiguredCredentials() {
        seeder("", "").run();
        seeder("admin@x.es", " ").run();

        verifyNoInteractions(repository, hasher);
    }

    @Test
    void doesNotDuplicateExistingAdmin() {
        when(repository.findByEmail("admin@x.es")).thenReturn(Optional.of(new User()));

        seeder("admin@x.es", "Secreta123").run();

        verify(repository, never()).save(any());
    }

    @Test
    void createsAdminWithHashedPassword() {
        when(repository.findByEmail("admin@x.es")).thenReturn(Optional.empty());
        when(hasher.hash("Secreta123")).thenReturn("HASH");

        seeder("admin@x.es", "Secreta123").run();

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repository).save(saved.capture());
        assertEquals("ADMIN", saved.getValue().getRole());
        assertEquals("admin@x.es", saved.getValue().getEmail());
        assertEquals("HASH", saved.getValue().getPassword());
    }
}
