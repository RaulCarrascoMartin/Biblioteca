package com.example.demo.persistance.repository;

import com.example.demo.domain.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Long> {
}
