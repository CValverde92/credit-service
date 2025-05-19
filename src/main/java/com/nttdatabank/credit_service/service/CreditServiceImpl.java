package com.nttdatabank.credit_service.service;

import com.nttdatabank.credit_service.dao.*;
import com.nttdatabank.credit_service.exception.BusinessException;
import com.nttdatabank.credit_service.model.*;
import com.nttdatabank.credit_service.repository.CreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CustomerService customerService;
    private final CreditRepository creditRepository;

    @Override
    public Mono<CreditResponse> create(CreditRequest request) {
        return customerService.findById(request.getCustomerId())
                .flatMap(customer -> validateCreditCreation(customer, request))
                .flatMap(validatedRequest -> {
                    Credit credit = mapRequestToEntity(validatedRequest);
                    return creditRepository.save(credit)
                            .map(this::mapEntityToResponse);
                });
    }

    private Mono<CreditRequest> validateCreditCreation(Customer customer, CreditRequest request) {
        if (request.getType().name().equals(CreditType.PERSONAL_LOAN.name()) &&
                customer.getType() == CustomerType.PERSONAL) {
            return creditRepository.existsByCustomerIdAndType(
                            request.getCustomerId(),
                            CreditType.PERSONAL_LOAN)
                    .flatMap(exists -> exists ?
                            Mono.error(new BusinessException("Personal customers can only have one personal loan")) :
                            Mono.just(request));
        }
        return Mono.just(request);
    }

    @Override
    public Mono<CreditResponse> update(String id, CreditUpdateRequest request) {
        return creditRepository.findById(id)
                .flatMap(existingCredit -> {
                    if (request.getStatus() != null) {
                        existingCredit.setStatus(CreditStatus.valueOf(request.getStatus().name()));
                    }
                    if (request.getCreditLimit() != null) {
                        existingCredit.setCreditLimit(request.getCreditLimit());
                    }
                    return creditRepository.save(existingCredit)
                            .map(this::mapEntityToResponse);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return creditRepository.deleteById(id);
    }

    @Override
    public Mono<CreditResponse> findById(String id) {
        return creditRepository.findById(id)
                .map(this::mapEntityToResponse);
    }

    @Override
    public Flux<CreditResponse> findAll() {
        return creditRepository.findAll()
                .map(this::mapEntityToResponse);
    }

    @Override
    public Flux<CreditResponse> findByCustomerId(String customerId) {
        return creditRepository.findByCustomerId(customerId)
                .map(this::mapEntityToResponse);
    }

    @Override
    public Mono<CreditResponse> chargeCredit(String creditId, CreditChargeRequest request) {
        return creditRepository.findById(creditId)
                .flatMap(credit -> {
                    if (credit.getType() != CreditType.PERSONAL_CREDIT_CARD &&
                            credit.getType() != CreditType.BUSINESS_CREDIT_CARD) {
                        return Mono.error(new BusinessException("Only credit cards can be charged"));
                    }

                    BigDecimal newUsed = credit.getUsedCredit().add(request.getAmount());
                    if (newUsed.compareTo(credit.getCreditLimit()) > 0) {
                        return Mono.error(new BusinessException("Credit limit exceeded"));
                    }

                    credit.setUsedCredit(newUsed);
                    return creditRepository.save(credit)
                            .map(this::mapEntityToResponse);
                });
    }

    @Override
    public Mono<CreditResponse> makePayment(String creditId, CreditPaymentRequest request) {
        return creditRepository.findById(creditId)
                .flatMap(credit -> {
                    BigDecimal newBalance = credit.getBalance().subtract(request.getAmount());
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        return Mono.error(new BusinessException("Payment exceeds current balance"));
                    }

                    credit.setBalance(newBalance);
                    return creditRepository.save(credit)
                            .map(this::mapEntityToResponse);
                });
    }

    private Credit mapRequestToEntity(CreditRequest request) {
        return Credit.builder()
                .customerId(request.getCustomerId())
                .type(CreditType.valueOf(request.getType().name()))
                .approvedAmount(request.getApprovedAmount())
                .balance(request.getApprovedAmount())
                .interestRate(request.getInterestRate())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(request.getTermMonths()))
                .status(CreditStatus.ACTIVE)
                .creditLimit(request.getCreditLimit())
                .usedCredit(BigDecimal.ZERO)
                .build();
    }

    private CreditResponse mapEntityToResponse(Credit credit) {
        CreditResponse response = new CreditResponse();
        response.setId(credit.getId());
        response.setCustomerId(credit.getCustomerId());
        response.setType(credit.getType().name());
        response.setCreditNumber(credit.getCreditNumber());
        response.setApprovedAmount(credit.getApprovedAmount());
        response.setBalance(credit.getBalance());
        response.setInterestRate(credit.getInterestRate());
        response.setStartDate(credit.getStartDate());
        response.setEndDate(credit.getEndDate());
        response.setStatus(credit.getStatus().name());
        response.setMonthlyPayment(calculateMonthlyPayment(credit));
        response.setCreditLimit(credit.getCreditLimit());
        response.setUsedCredit(credit.getUsedCredit());
        return response;
    }

    private BigDecimal calculateMonthlyPayment(Credit credit) {
        if (credit.getType() == CreditType.PERSONAL_CREDIT_CARD ||
                credit.getType() == CreditType.BUSINESS_CREDIT_CARD) {
            return BigDecimal.ZERO;
        }
        return credit.getApprovedAmount()
                .multiply(BigDecimal.ONE.add(
                        credit.getInterestRate().divide(BigDecimal.valueOf(100))))
                .divide(BigDecimal.valueOf(credit.getTermMonths().intValue()), 2, RoundingMode.HALF_UP);
    }

}
