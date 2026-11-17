package com.example.demo.loans.internal.controller.mapper;

import com.example.demo.loans.dto.LoanResponseDto;
import com.example.demo.loans.internal.controller.dto.LoanResponse;
import org.mapstruct.Mapper;

import java.util.List;

/** Convierte el DTO público del servicio en el DTO web. Solo transforma datos. */
@Mapper(componentModel = "spring")
public interface LoanWebMapper {

    LoanResponse toResponse(LoanResponseDto dto);

    List<LoanResponse> toResponses(List<LoanResponseDto> dtos);
}
