package com.example.demo.security;

import com.example.demo.domain.entity.Usuario;
import com.example.demo.persistance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${admin.email:}") String adminEmail,
                       @Value("${admin.password:}") String adminPassword) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRol("ADMIN");
        usuarioRepository.save(admin);
    }
}