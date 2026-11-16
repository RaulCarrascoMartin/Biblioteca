package com.example.demo.libro.internal.application;

import com.example.demo.libro.LibroModule;
import com.example.demo.libro.dto.LibroResponseDTO;
import com.example.demo.libro.internal.domain.LibroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/** Implementación de la API pública: solo delega en el servicio. */
@Component
@RequiredArgsConstructor
public class LibroModuleImpl implements LibroModule {

    private final LibroService libroService;

    @Override
    public Optional<LibroResponseDTO> obtenerPorId(Long id) {
        return libroService.obtenerPorId(id);
    }

    @Override
    public Map<Long, LibroResponseDTO> obtenerPorIds(Collection<Long> ids) {
        return libroService.obtenerPorIds(ids);
    }

    @Override
    public LibroResponseDTO reservar(Long id) {
        return libroService.reservar(id);
    }

    @Override
    public LibroResponseDTO liberar(Long id) {
        return libroService.liberar(id);
    }
}
