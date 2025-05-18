package com.nttdatabank.credit_service.repository;

import com.nttdatabank.credit_service.dao.Credit;
import com.nttdatabank.credit_service.dao.CreditType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditRepository extends ReactiveMongoRepository<Credit, String> {
    Flux<Credit> findByCustomerId(String customerId);

    Mono<Boolean> existsByCustomerIdAndType(String customerId, CreditType type);
}
