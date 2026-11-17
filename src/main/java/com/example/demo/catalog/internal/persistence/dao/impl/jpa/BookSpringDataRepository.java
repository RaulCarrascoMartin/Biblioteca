package com.example.demo.catalog.internal.persistence.dao.impl.jpa;

import com.example.demo.catalog.internal.persistence.dao.entity.BookEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookSpringDataRepository extends JpaRepository<BookEntity, Long> {

    Optional<BookEntity> findByIsbn(String isbn);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BookEntity b WHERE b.id = :id")
    Optional<BookEntity> findByIdWithLock(@Param("id") Long id);
}
