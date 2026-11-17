package com.example.demo.loans.internal.persistence.repository.mapper;

import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.loans.internal.persistence.dao.entity.LoanEntity;
import org.springframework.stereotype.Component;

/** Convierte entre la entidad JPA y el modelo de dominio. Sin reglas de negocio. */
@Component
public class LoanPersistenceMapper {

    /** Reconstruye el modelo con su constructor: el dominio vuelve a comprobar sus invariantes. */
    public Loan toModel(LoanEntity entity) {
        return new Loan(entity.getId(), entity.getBookId(), entity.getUserId(), entity.getLoanDate(),
                entity.getReturnDate());
    }

    /** Para modelos sin id; rechaza los que ya lo traen. */
    public LoanEntity toNewEntity(Loan loan) {
        if (loan.id() != null) {
            throw new IllegalArgumentException("A new loan cannot have an id");
        }
        return new LoanEntity(loan.bookId(), loan.userId(), loan.loanDate(), loan.returnDate());
    }

    /** Copia el estado mutable del modelo (la devolución) sobre una entidad gestionada. */
    public void updateManagedEntity(Loan loan, LoanEntity entity) {
        entity.setReturnDate(loan.returnDate());
    }
}
