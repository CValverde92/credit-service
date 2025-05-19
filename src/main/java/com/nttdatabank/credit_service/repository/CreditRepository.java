package com.nttdatabank.credit_service.repository;

import com.nttdatabank.credit_service.dao.Credit;
import com.nttdatabank.credit_service.model.CreditRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive MongoDB repository for Credit operations
 */
public interface CreditRepository extends ReactiveMongoRepository<Credit, String> {
    Flux<Credit> findByCustomerId(String customerId);

    Mono<Boolean> existsByCustomerIdAndType(String customerId, CreditRequest.TypeEnum type);
}
