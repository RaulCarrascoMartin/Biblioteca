package com.example.demo.libro.internal.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaLibroRepository extends JpaRepository<LibroEntity, Long> {

    Optional<LibroEntity> findByIsbn(String isbn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LibroEntity l WHERE l.id = :id")
    Optional<LibroEntity> findByIdWithLock(@Param("id") Long id);

}
