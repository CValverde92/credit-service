package com.nttdatabank.credit_service.service;

import com.nttdatabank.credit_service.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service interface for credit operations
 */
public interface CreditService {
    Mono<CreditResponse> create(CreditRequest request);

    Mono<CreditResponse> update(String id, CreditUpdateRequest request);

    Mono<Void> delete(String id);

    Mono<CreditResponse> findById(String id);

    Flux<CreditResponse> findAll();

    Flux<CreditResponse> findByCustomerId(String customerId);

    Mono<CreditResponse> chargeCredit(String creditId, CreditChargeRequest request);

    Mono<CreditResponse> makePayment(String creditId, CreditPaymentRequest request);
}
