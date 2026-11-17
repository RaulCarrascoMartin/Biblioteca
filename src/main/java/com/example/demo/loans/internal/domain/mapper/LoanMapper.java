package com.example.demo.loans.internal.domain.mapper;

import com.example.demo.catalog.dto.BookResponseDto;
import com.example.demo.loans.dto.LoanResponseDto;
import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
import com.example.demo.loans.internal.domain.model.Loan;
import com.example.demo.users.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Convierte entre el modelo {@link Loan} y sus DTO. La respuesta se compone con el préstamo y los DTO
 * públicos de {@code catalog} y {@code users}: el préstamo solo guarda ids y nunca ve sus modelos.
 * Solo transforma datos, sin reglas de negocio.
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
    LoanResponseDto toResponse(Loan loan, BookResponseDto book, UserResponseDto user);

    /** Las fechas las fija el servicio. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loanDate", ignore = true)
    @Mapping(target = "returnDate", ignore = true)
    Loan toModel(CreateLoanDto dto);
}
