package com.example.demo.loans.internal.application;

import com.example.demo.loans.internal.domain.repository.LoanRepository;
import com.example.demo.users.UserInUseChecker;
import org.springframework.stereotype.Component;

/**
 * Adaptador hacia {@code users}: un usuario con préstamos registrados no puede eliminarse (se conserva
 * el historial). Usa el repositorio propio por el mismo motivo que {@link BookWithLoansChecker}.
 */
@Component
public class UserWithLoansChecker implements UserInUseChecker {

    private final LoanRepository loanRepository;

    public UserWithLoansChecker(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public boolean isInUse(Long userId) {
        return loanRepository.existsByUserId(userId);
    }
}
