package com.nttdatabank.credit_service.controller;

import com.nttdatabank.credit_service.dao.Transaction;
import com.nttdatabank.credit_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Comparator;

/**
 * Controller for managing credit card transactions
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/credits/{creditId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    // Retrieves all transactions for a credit account
    @GetMapping
    public Flux<Transaction> getTransactions(@PathVariable String creditId) {
        return transactionService.getTransactionsByCreditId(creditId);
    }

    // Returns transactions sorted by date (newest first)
    @GetMapping("/sorted")
    public Flux<Transaction> getSortedTransactions(@PathVariable String creditId) {
        return transactionService.findByCreditId(creditId)
                .sort(Comparator.comparing(Transaction::getDate).reversed());
    }
}
