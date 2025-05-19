package com.nttdatabank.credit_service.service.impl;

import com.nttdatabank.credit_service.dao.Customer;
import com.nttdatabank.credit_service.dao.CustomerStatus;
import com.nttdatabank.credit_service.dao.CustomerType;
import com.nttdatabank.credit_service.exception.BusinessException;
import com.nttdatabank.credit_service.repository.CustomerRepository;
import com.nttdatabank.credit_service.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implements customer data operations and validations
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    // Finds customer by ID or throws exception if not found
    @Override
    public Mono<Customer> findById(String id) {
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new BusinessException("Customer not found with ID: " + id)
                ));
    }

    // Gets customer type (BUSINESS/PERSONAL) or throws exception
    @Override
    public Mono<CustomerType> getCustomerType(String customerId) {
        return customerRepository.findById(customerId)
                .map(Customer::getType)
                .switchIfEmpty(Mono.error(
                        new BusinessException("Customer does not exist")
                ));
    }

    // Checks if customer account is in ACTIVE status
    @Override
    public Mono<Boolean> isCustomerActive(String customerId) {
        return customerRepository.existsByIdAndStatus(
                customerId,
                CustomerStatus.ACTIVE
        ).switchIfEmpty(Mono.just(false));
    }
}
