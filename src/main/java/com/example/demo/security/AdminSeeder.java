package com.example.demo.security;

import com.example.demo.domain.entity.Usuario;
import com.example.demo.domain.port.PasswordHasher;
import com.example.demo.domain.port.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UsuarioRepository usuarioRepository,
                       PasswordHasher passwordHasher,
                       @Value("${admin.email:}") String adminEmail,
                       @Value("${admin.password:}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }
        if (usuarioRepository.findByEmail(adminEmail).isPresent()) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNombre("Administrador");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordHasher.encode(adminPassword));
        admin.setRol("ADMIN");
        usuarioRepository.save(admin);
    }
}