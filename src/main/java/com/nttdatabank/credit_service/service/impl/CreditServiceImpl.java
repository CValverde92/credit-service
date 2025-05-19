package com.nttdatabank.credit_service.service.impl;

import com.nttdatabank.credit_service.dao.*;
import com.nttdatabank.credit_service.exception.BusinessException;
import com.nttdatabank.credit_service.model.*;
import com.nttdatabank.credit_service.repository.CreditRepository;
import com.nttdatabank.credit_service.repository.TransactionRepository;
import com.nttdatabank.credit_service.service.CreditService;
import com.nttdatabank.credit_service.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implementation of credit service operations
 **/
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CustomerService customerService;
    private final CreditRepository creditRepository;
    private final TransactionRepository transactionRepository;

    // Create a new credit account after validation
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

    // Validate credit creation rules based on customer type
    private Mono<CreditRequest> validateCreditCreation(Customer customer, CreditRequest request) {
        if (request.getType() == null) return Mono.error(new BusinessException("Credit type cannot be null"));

        if (customer.getType() == CustomerType.BUSINESS) {
            if (request.getType() == CreditRequest.TypeEnum.PERSONAL_LOAN) {
                return Mono.error(new BusinessException("Business clients cannot get personal loans"));
            }
        }

        if (customer.getType() == CustomerType.BUSINESS) {
            return Mono.just(request); // Businesses can have multiple credits
        }
        if (request.getType() == CreditRequest.TypeEnum.PERSONAL_LOAN) {
            return creditRepository.existsByCustomerIdAndType(
                            request.getCustomerId(),
                            request.getType())
                    .flatMap(exists -> exists ?
                            Mono.error(new BusinessException("Personal customers can only have one personal loan")) :
                            Mono.just(request)
                    );
        }
        return Mono.just(request);
    }

    // Update an existing credit account
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

    // Delete a credit account by ID
    @Override
    public Mono<Void> delete(String id) {
        return creditRepository.deleteById(id);
    }

    // Find credit account by ID
    @Override
    public Mono<CreditResponse> findById(String id) {
        return creditRepository.findById(id)
                .map(this::mapEntityToResponse);
    }

    // Retrieve all credit accounts
    @Override
    public Flux<CreditResponse> findAll() {
        return creditRepository.findAll()
                .map(this::mapEntityToResponse);
    }

    // Find all credits for a specific customer
    @Override
    public Flux<CreditResponse> findByCustomerId(String customerId) {
        return creditRepository.findByCustomerId(customerId)
                .map(this::mapEntityToResponse);
    }

    // Process a charge to a credit card
    @Override
    public Mono<CreditResponse> chargeCredit(String creditId, CreditChargeRequest request) {
        return creditRepository.findById(creditId)
                .flatMap(credit -> {
                    if (credit.getStatus() != CreditStatus.ACTIVE) {
                        return Mono.error(new BusinessException("Credit is not active"));
                    }
                    if (credit.getType() != CreditRequest.TypeEnum.PERSONAL_CREDIT_CARD &&
                            credit.getType() != CreditRequest.TypeEnum.BUSINESS_CREDIT_CARD) {
                        return Mono.error(new BusinessException("Only credit cards can be charged"));
                    }

                    BigDecimal newUsed = credit.getUsedCredit().add(request.getAmount());
                    if (newUsed.compareTo(credit.getCreditLimit()) > 0) {
                        return Mono.error(new BusinessException("Credit limit exceeded"));
                    }
                    // Update credit
                    credit.setUsedCredit(newUsed);
                    // Create transaction
                    Transaction transaction = Transaction.builder()
                            .creditId(creditId)
                            .amount(request.getAmount())
                            .type(TransactionType.CHARGE)
                            .date(LocalDateTime.now())
                            .description("Card charge: " + request.getDescription())
                            .build();

                    // Save both in parallel (transaction and credit)
                    return Mono.zip(
                            transactionRepository.save(transaction),
                            creditRepository.save(credit)
                    ).map(tuple -> mapEntityToResponse(tuple.getT2()));
                });
    }

    // Process a payment to a credit account
    @Override
    public Mono<CreditResponse> makePayment(String creditId, CreditPaymentRequest request) {
        return creditRepository.findById(creditId)
                .flatMap(credit -> {
                    if (credit.getStatus() != CreditStatus.ACTIVE) {
                        return Mono.error(new BusinessException("Credit is not active"));
                    }
                    BigDecimal newBalance = credit.getBalance().subtract(request.getAmount());
                    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                        return Mono.error(new BusinessException("Payment exceeds current balance"));
                    }
                    // Update credit
                    credit.setBalance(newBalance);
                    // Create transaction
                    Transaction transaction = Transaction.builder()
                            .creditId(creditId)
                            .amount(request.getAmount())
                            .type(TransactionType.PAYMENT)
                            .date(LocalDateTime.now())
                            .description("Pago: " + request.getPaymentMethod())
                            .build();

                    // Save both
                    return Mono.zip(
                            transactionRepository.save(transaction),
                            creditRepository.save(credit)
                    ).map(tuple -> mapEntityToResponse(tuple.getT2()));
                });
    }

    // Map request DTO to credit entity
    private Credit mapRequestToEntity(CreditRequest request) {
        return Credit.builder()
                .customerId(request.getCustomerId())
                .type(request.getType())
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

    // Map credit entity to response DTO
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

    // Calculate monthly payment amount for loans
    private BigDecimal calculateMonthlyPayment(Credit credit) {
        if (credit.getType() == CreditRequest.TypeEnum.PERSONAL_CREDIT_CARD ||
                credit.getType() == CreditRequest.TypeEnum.BUSINESS_CREDIT_CARD) {
            return BigDecimal.ZERO; // Credit cards don't have fixed monthly payments
        }
        return credit.getApprovedAmount()
                .multiply(BigDecimal.ONE.add(
                        credit.getInterestRate().divide(BigDecimal.valueOf(100))))
                .divide(BigDecimal.valueOf(credit.getTermMonths().intValue()), 2, RoundingMode.HALF_UP);
    }

}
