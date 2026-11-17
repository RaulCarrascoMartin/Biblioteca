package com.example.demo.loans.internal.controller;

import com.example.demo.loans.internal.controller.dto.LoanResponse;
import com.example.demo.loans.internal.controller.mapper.LoanWebMapper;
import com.example.demo.loans.internal.domain.dto.LoanDto;
import com.example.demo.loans.internal.usecase.CreateLoanUseCase;
import com.example.demo.loans.internal.usecase.GetAllLoansUseCase;
import com.example.demo.loans.internal.usecase.GetLoanUseCase;
import com.example.demo.loans.internal.usecase.GetOwnLoansUseCase;
import com.example.demo.loans.internal.usecase.ReturnLoanUseCase;
import com.example.demo.shared.Requester;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Traduce HTTP a llamadas al caso de uso y mapea {@link LoanDto} a DTO web. La autorización la deciden
 * los casos de uso.
 */
@RestController
public class LoanController implements LoanHttpApi {

    private final GetAllLoansUseCase getAllLoansUseCase;
    private final GetOwnLoansUseCase getOwnLoansUseCase;
    private final GetLoanUseCase getLoanUseCase;
    private final CreateLoanUseCase createLoanUseCase;
    private final ReturnLoanUseCase returnLoanUseCase;
    private final LoanWebMapper mapper;

    public LoanController(GetAllLoansUseCase getAllLoansUseCase,
                          GetOwnLoansUseCase getOwnLoansUseCase,
                          GetLoanUseCase getLoanUseCase,
                          CreateLoanUseCase createLoanUseCase,
                          ReturnLoanUseCase returnLoanUseCase,
                          LoanWebMapper mapper) {
        this.getAllLoansUseCase = getAllLoansUseCase;
        this.getOwnLoansUseCase = getOwnLoansUseCase;
        this.getLoanUseCase = getLoanUseCase;
        this.createLoanUseCase = createLoanUseCase;
        this.returnLoanUseCase = returnLoanUseCase;
        this.mapper = mapper;
    }

    @Override
    public List<LoanResponse> getAll(Requester requester) {
        return mapper.toResponses(getAllLoansUseCase.execute(requester));
    }

    @Override
    public List<LoanResponse> getOwn(Requester requester) {
        return mapper.toResponses(getOwnLoansUseCase.execute(requester));
    }

    @Override
    public LoanResponse getById(Requester requester, Long id) {
        return mapper.toResponse(getLoanUseCase.execute(requester, id));
    }

    @Override
    public ResponseEntity<LoanResponse> create(Requester requester, Long bookId) {
        LoanDto created = createLoanUseCase.execute(requester, bookId);
        return ResponseEntity.created(URI.create(BASE_PATH + "/" + created.id()))
                .body(mapper.toResponse(created));
    }

    @Override
    public LoanResponse returnLoan(Requester requester, Long id) {
        return mapper.toResponse(returnLoanUseCase.execute(requester, id));
    }
}
