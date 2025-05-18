package com.nttdatabank.credit_service.repository;

import com.nttdatabank.credit_service.dao.Customer;
import com.nttdatabank.credit_service.dao.CustomerStatus;
import com.nttdatabank.credit_service.dao.CustomerType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {
    Mono<Customer> findByDocumentNumber(String documentNumber);

    Mono<Boolean> existsByDocumentNumberAndType(String documentNumber, CustomerType type);

    Mono<Boolean> existsByIdAndStatus(String id, CustomerStatus status);
}
