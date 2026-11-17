package com.example.demo.loans.internal.domain.mapper;

import com.example.demo.catalog.dto.BookSummary;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.users.dto.UserSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Convierte el modelo {@link Loan} en {@link LoanDto}, componiéndolo con los DTO públicos de {@code catalog}
 * y {@code users}: el préstamo solo guarda ids y nunca ve sus modelos. Sin reglas de negocio.
 */
@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "id", source = "loan.id")
    @Mapping(target = "loanDate", source = "loan.loanDate")
    @Mapping(target = "returnDate", source = "loan.returnDate")
    @Mapping(target = "book", source = "book")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userEmail", source = "user.email")
    LoanDto toDto(Loan loan, BookSummary book, UserSummary user);
}
