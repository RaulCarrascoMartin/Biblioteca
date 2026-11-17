package com.example.demo.loans.internal.controller;

import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.loans.internal.controller.mapper.LoanWebMapper;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.usecase.LoanUseCase;
import com.example.demo.shared.Requester;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Traduce HTTP a llamadas al caso de uso y mapea {@link LoanDto} a DTO web. La autorización la decide
 * {@link LoanUseCase}.
 */
@RestController
public class LoanController implements LoanHttpApi {

    private final LoanUseCase loanUseCase;
    private final LoanWebMapper mapper;

    public LoanController(LoanUseCase loanUseCase, LoanWebMapper mapper) {
        this.loanUseCase = loanUseCase;
        this.mapper = mapper;
    }

    @Override
    public List<LoanResponse> getAll(Requester requester) {
        return mapper.toResponses(loanUseCase.getAll(requester));
    }

    @Override
    public List<LoanResponse> getOwn(Requester requester) {
        return mapper.toResponses(loanUseCase.getOwn(requester));
    }

    @Override
    public LoanResponse getById(Requester requester, Long id) {
        return mapper.toResponse(loanUseCase.getById(requester, id));
    }

    @Override
    public ResponseEntity<LoanResponse> create(Requester requester, Long bookId) {
        LoanDto created = loanUseCase.create(requester, bookId);
        return ResponseEntity.created(URI.create(BASE_PATH + "/" + created.id()))
                .body(mapper.toResponse(created));
    }

    @Override
    public LoanResponse returnLoan(Requester requester, Long id) {
        return mapper.toResponse(loanUseCase.returnLoan(requester, id));
    }
}
