package com.example.demo.loans.internal.usecase;

import com.example.demo.loans.dto.LoanResponseDto;
import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.loans.internal.controller.mapper.LoanWebMapper;
import com.example.demo.loans.internal.domain.dto.CreateLoanDto;
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
    private final LoanWebMapper webMapper;

    public LoanUseCase(LoanService loanService, LoanWebMapper webMapper) {
        this.loanService = loanService;
        this.webMapper = webMapper;
    }

    public List<LoanResponse> getAll(Requester requester) {
        requireAdmin(requester);
        return webMapper.toResponses(loanService.getAll());
    }

    public List<LoanResponse> getOwn(Requester requester) {
        requireAuthenticated(requester);
        return webMapper.toResponses(loanService.getByUserId(requester.id()));
    }

    public LoanResponse getById(Requester requester, Long id) {
        LoanResponseDto loan = loanService.getById(id);
        requireAdminOrOwner(requester, loan);
        return webMapper.toResponse(loan);
    }

    public LoanResponse create(Requester requester, Long bookId) {
        requireAuthenticated(requester);
        return webMapper.toResponse(loanService.create(new CreateLoanDto(bookId, requester.id())));
    }

    public LoanResponse returnLoan(Requester requester, Long id) {
        requireAdminOrOwner(requester, loanService.getById(id));
        return webMapper.toResponse(loanService.returnLoan(id));
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

    private static void requireAdminOrOwner(Requester requester, LoanResponseDto loan) {
        if (!requester.isAdmin() && !requester.isOwner(loan.userId())) {
            throw new ForbiddenException();
        }
    }
}
