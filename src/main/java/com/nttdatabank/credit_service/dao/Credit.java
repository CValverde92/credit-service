package com.nttdatabank.credit_service.dao;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Document(collection = "credits")
public class Credit {
    @Id
    private String id;
    private String customerId;
    private CreditType type;
    private String creditNumber;
    private BigDecimal approvedAmount;
    private BigDecimal balance;
    private BigDecimal interestRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private CreditStatus status;
    private BigDecimal monthlyPayment;
    private BigDecimal termMonths;
    private BigDecimal creditLimit;
    private BigDecimal usedCredit;

}
