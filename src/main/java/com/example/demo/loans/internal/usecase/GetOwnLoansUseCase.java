package com.example.demo.loans.internal.usecase;

import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.service.LoanService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import java.util.List;
import org.springframework.stereotype.Component;

/** Lista los préstamos del solicitante. Política: cualquier usuario con identidad. */
@Component
public class GetOwnLoansUseCase {

    private final LoanService loanService;

    public GetOwnLoansUseCase(LoanService loanService) {
        this.loanService = loanService;
    }

    public List<LoanDto> execute(Requester requester) {
        if (requester.id() == null) {
            throw new ForbiddenException();
        }
        return loanService.getByUserId(requester.id());
    }
}
