package com.nttdatabank.credit_service.exception;
/**
 * Custom exception for business rule violations
 **/
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
