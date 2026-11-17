package com.example.demo.loans.internal.persistence;

import com.example.demo.loans.internal.domain.model.Loan;
import org.mapstruct.Mapper;

/** Convierte entre la entidad JPA y el modelo de dominio. */
@Mapper(componentModel = "spring")
public interface LoanEntityMapper {

    Loan toModel(LoanEntity entity);

    LoanEntity toEntity(Loan loan);
}
