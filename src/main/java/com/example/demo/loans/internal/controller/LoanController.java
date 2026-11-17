package com.example.demo.loans.internal.controller;

import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.loans.internal.usecase.LoanUseCase;
import com.example.demo.shared.Requester;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Traduce HTTP a llamadas al caso de uso. La autorización la decide {@link LoanUseCase}. */
@RestController
public class LoanController implements LoanHttpApi {

    private final LoanUseCase loanUseCase;

    public LoanController(LoanUseCase loanUseCase) {
        this.loanUseCase = loanUseCase;
    }

    @Override
    public List<LoanResponse> getAll(Requester requester) {
        return loanUseCase.getAll(requester);
    }

    @Override
    public List<LoanResponse> getOwn(Requester requester) {
        return loanUseCase.getOwn(requester);
    }

    @Override
    public LoanResponse getById(Requester requester, Long id) {
        return loanUseCase.getById(requester, id);
    }

    @Override
    public LoanResponse create(Requester requester, Long bookId) {
        return loanUseCase.create(requester, bookId);
    }

    @Override
    public LoanResponse returnLoan(Requester requester, Long id) {
        return loanUseCase.returnLoan(requester, id);
    }
}
