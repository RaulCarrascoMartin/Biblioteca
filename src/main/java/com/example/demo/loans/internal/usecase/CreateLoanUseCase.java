package com.example.demo.loans.internal.usecase;

import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.service.LoanService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

/** Presta un libro a nombre del solicitante. Política: cualquier usuario con identidad. */
@Component
public class CreateLoanUseCase {

    private final LoanService loanService;

    public CreateLoanUseCase(LoanService loanService) {
        this.loanService = loanService;
    }

    public LoanDto execute(Requester requester, Long bookId) {
        if (requester.id() == null) {
            throw new ForbiddenException();
        }
        return loanService.create(new CreateLoanDto(bookId, requester.id()));
    }
}
