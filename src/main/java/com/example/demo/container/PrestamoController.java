package com.example.demo.container;


import com.example.demo.domain.entity.Prestamo;
import com.example.demo.domain.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @GetMapping
    public List<Prestamo> obtenerTodos(){
        return prestamoService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prestamo> obtenerPorId(@PathVariable Long id){
        return prestamoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestParam Long libroId, @RequestParam Long usuarioId){
        try {
            Prestamo prestamo = prestamoService.crearPrestamo(libroId, usuarioId);
            return ResponseEntity.ok(prestamo);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<?> devolver(@PathVariable Long id){
        try {
            Prestamo prestamo = prestamoService.devolverLibro(id);
            return ResponseEntity.ok(prestamo);
        }catch (RuntimeException e){
            return ResponseEntity.notFound().build();
        }
    }

}
