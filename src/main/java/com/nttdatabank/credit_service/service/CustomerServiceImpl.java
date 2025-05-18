package com.nttdatabank.credit_service.service;

import com.nttdatabank.credit_service.dao.Customer;
import com.nttdatabank.credit_service.dao.CustomerStatus;
import com.nttdatabank.credit_service.dao.CustomerType;
import com.nttdatabank.credit_service.exception.BusinessException;
import com.nttdatabank.credit_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Mono<Customer> findById(String id) {
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new BusinessException("Cliente no encontrado con ID: " + id)
                ));
    }

    @Override
    public Mono<CustomerType> getCustomerType(String customerId) {
        return customerRepository.findById(customerId)
                .map(Customer::getType)
                .switchIfEmpty(Mono.error(
                        new BusinessException("Cliente no existe")
                ));
    }

    @Override
    public Mono<Boolean> isCustomerActive(String customerId) {
        return customerRepository.existsByIdAndStatus(
                customerId,
                CustomerStatus.ACTIVE
        ).switchIfEmpty(Mono.just(false));
    }
}
