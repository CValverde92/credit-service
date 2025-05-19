package com.nttdatabank.credit_service.repository;

import com.nttdatabank.credit_service.dao.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Repository for transaction data operations
 **/
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByCreditId(String creditId);
}
