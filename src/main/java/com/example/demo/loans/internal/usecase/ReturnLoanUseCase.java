package com.example.demo.loans.internal.usecase;

import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.service.LoanService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

/** Devuelve un libro. Política: ADMIN o dueño del préstamo. */
@Component
public class ReturnLoanUseCase {

    private final LoanService loanService;

    public ReturnLoanUseCase(LoanService loanService) {
        this.loanService = loanService;
    }

    public LoanDto execute(Requester requester, Long id) {
        LoanDto loan = loanService.getById(id);
        if (!requester.isAdmin() && !requester.isOwner(loan.userId())) {
            throw new ForbiddenException();
        }
        return loanService.returnLoan(id);
    }
}
