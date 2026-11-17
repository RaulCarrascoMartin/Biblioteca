package com.example.demo.config;

import com.example.demo.users.UsersModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial si hay credenciales configuradas. Solo lee la configuración: la
 * regla (rol, hash, unicidad) la aplica {@link UsersModule#ensureAdmin}.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsersModule usersModule;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UsersModule usersModule,
                       @Value("${admin.email:}") String adminEmail,
                       @Value("${admin.password:}") String adminPassword) {
        this.usersModule = usersModule;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }
        usersModule.ensureAdmin(adminEmail, adminPassword);
    }
}
