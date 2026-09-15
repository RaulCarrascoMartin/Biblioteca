package com.example.demo.domain.service;

import com.example.demo.domain.dto.UsuarioUpdateDTO;
import com.example.demo.domain.entity.Usuario;
import com.example.demo.persistance.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodos(){
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerPorId(Long id){
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> obtenerPorEmail(String email){
        return usuarioRepository.findByEmail(email);
    }

    public Usuario guardar(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> actualizar(Long id, UsuarioUpdateDTO usuarioNuevo) {
        return usuarioRepository.findById(id).map(existente -> {
            if (!existente.getEmail().equalsIgnoreCase(usuarioNuevo.getEmail())) {
                if (usuarioRepository.findByEmail(usuarioNuevo.getEmail()).isPresent()) {
                    throw new IllegalArgumentException("El email ya está registrado");
                }
            }
            existente.setNombre(usuarioNuevo.getNombre());
            existente.setEmail(usuarioNuevo.getEmail());

            if (usuarioNuevo.getPassword() != null && !usuarioNuevo.getPassword().isBlank()) {
                existente.setPassword(passwordEncoder.encode(usuarioNuevo.getPassword()));
            }

            return usuarioRepository.save(existente);
        });
    }

    public void eliminar(Long id){
        usuarioRepository.deleteById(id);
    }

}