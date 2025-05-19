package com.nttdatabank.credit_service.service.impl;

import com.nttdatabank.credit_service.dao.Transaction;
import com.nttdatabank.credit_service.repository.TransactionRepository;
import com.nttdatabank.credit_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Service implementation for transaction operations
 **/
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    // Gets all transactions for specified credit account
    @Override
    public Flux<Transaction> getTransactionsByCreditId(String creditId) {
        return transactionRepository.findByCreditId(creditId);
    }

    // Alternative method to query transactions by credit ID
    @Override
    public Flux<Transaction> findByCreditId(String creditId) {
        return transactionRepository.findByCreditId(creditId);
    }
}
