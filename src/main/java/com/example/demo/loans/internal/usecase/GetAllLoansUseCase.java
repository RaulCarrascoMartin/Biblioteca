package com.example.demo.loans.internal.usecase;

import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.service.LoanService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import java.util.List;
import org.springframework.stereotype.Component;

/** Lista todos los préstamos. Política: solo ADMIN. */
@Component
public class GetAllLoansUseCase {

    private final LoanService loanService;

    public GetAllLoansUseCase(LoanService loanService) {
        this.loanService = loanService;
    }

    public List<LoanDto> execute(Requester requester) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException();
        }
        return loanService.getAll();
    }
}
