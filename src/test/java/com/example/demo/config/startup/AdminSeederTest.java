package com.example.demo.config.startup;

import com.example.demo.users.UsersModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private UsersModule usersModule;

    private AdminSeeder seeder(String email, String password) {
        return new AdminSeeder(usersModule, email, password);
    }

    @Test
    void doesNothingWithoutConfiguredCredentials() {
        seeder("", "").run();
        seeder("admin@x.es", " ").run();
        seeder(" ", "Secreta123").run();

        verifyNoInteractions(usersModule);
    }

    @Test
    void ensuresAdminWithConfiguredCredentials() {
        seeder("admin@x.es", "Secreta123").run();

        verify(usersModule).ensureAdmin("admin@x.es", "Secreta123");
    }
}
