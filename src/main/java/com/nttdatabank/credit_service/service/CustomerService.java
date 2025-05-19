package com.nttdatabank.credit_service.service;

import com.nttdatabank.credit_service.dao.Customer;
import com.nttdatabank.credit_service.dao.CustomerType;
import reactor.core.publisher.Mono;

/**
 * Provides customer information and status checks
 */
public interface CustomerService {
    Mono<Customer> findById(String id);

    Mono<CustomerType> getCustomerType(String customerId);

    Mono<Boolean> isCustomerActive(String customerId);
}
