package com.example.demo.loans.internal.usecase;

import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.domain.service.LoanService;
import com.example.demo.shared.ForbiddenException;
import com.example.demo.shared.Requester;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Flujos de usuario sobre los préstamos y su política de acceso: listar todos, solo ADMIN; listar los
 * propios y crear, cualquier autenticado (el préstamo se crea a nombre del solicitante); obtener y
 * devolver, ADMIN o dueño del préstamo. Las reglas de negocio las aplica {@link LoanService}.
 */
@Component
public class LoanUseCase {

    private final LoanService loanService;

    public LoanUseCase(LoanService loanService) {
        this.loanService = loanService;
    }

    public List<LoanDto> getAll(Requester requester) {
        requireAdmin(requester);
        return loanService.getAll();
    }

    public List<LoanDto> getOwn(Requester requester) {
        requireAuthenticated(requester);
        return loanService.getByUserId(requester.id());
    }

    public LoanDto getById(Requester requester, Long id) {
        LoanDto loan = loanService.getById(id);
        requireAdminOrOwner(requester, loan);
        return loan;
    }

    public LoanDto create(Requester requester, Long bookId) {
        requireAuthenticated(requester);
        return loanService.create(new CreateLoanDto(bookId, requester.id()));
    }

    public LoanDto returnLoan(Requester requester, Long id) {
        requireAdminOrOwner(requester, loanService.getById(id));
        return loanService.returnLoan(id);
    }

    private static void requireAdmin(Requester requester) {
        if (!requester.isAdmin()) {
            throw new ForbiddenException();
        }
    }

    private static void requireAuthenticated(Requester requester) {
        if (requester.id() == null) {
            throw new ForbiddenException();
        }
    }

    private static void requireAdminOrOwner(Requester requester, LoanDto loan) {
        if (!requester.isAdmin() && !requester.isOwner(loan.userId())) {
            throw new ForbiddenException();
        }
    }
}
