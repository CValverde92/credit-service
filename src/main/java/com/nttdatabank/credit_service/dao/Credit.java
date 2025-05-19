package com.nttdatabank.credit_service.dao;

import com.nttdatabank.credit_service.model.CreditRequest;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Document(collection = "credits")
public class Credit {
    @Id
    private String id;
    private String customerId;
    @Field(targetType = FieldType.STRING)
    private CreditRequest.TypeEnum type;
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
