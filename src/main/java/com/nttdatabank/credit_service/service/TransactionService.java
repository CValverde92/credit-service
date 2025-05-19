package com.nttdatabank.credit_service.service;

import com.nttdatabank.credit_service.dao.Transaction;
import reactor.core.publisher.Flux;

/**
 * Service interface for transaction operations
 **/
public interface TransactionService {
    Flux<Transaction> getTransactionsByCreditId(String creditId);

    Flux<Transaction> findByCreditId(String creditId);
}
