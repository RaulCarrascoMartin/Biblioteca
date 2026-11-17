package com.example.demo.loans.internal.controller.mapper;

import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import org.mapstruct.Mapper;

import java.util.List;

/** Convierte el DTO de dominio en el DTO web. Solo transforma datos. */
@Mapper(componentModel = "spring")
public interface LoanWebMapper {

    LoanResponse toResponse(LoanDto dto);

    List<LoanResponse> toResponses(List<LoanDto> dtos);
}
