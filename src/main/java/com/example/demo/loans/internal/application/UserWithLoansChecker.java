package com.example.demo.loans.internal.application;

import com.example.demo.loans.internal.domain.service.LoanQueryService;
import com.example.demo.users.spi.UserInUseChecker;
import org.springframework.stereotype.Component;

/**
 * Adaptador hacia {@code users}: un usuario con préstamos registrados no puede eliminarse (se conserva
 * el historial). Usa {@link LoanQueryService} por el mismo motivo que {@link BookWithLoansChecker}.
 */
@Component
public class UserWithLoansChecker implements UserInUseChecker {

    private final LoanQueryService loanQueryService;

    public UserWithLoansChecker(LoanQueryService loanQueryService) {
        this.loanQueryService = loanQueryService;
    }

    @Override
    public boolean isInUse(Long userId) {
        return loanQueryService.existsByUserId(userId);
    }
}
