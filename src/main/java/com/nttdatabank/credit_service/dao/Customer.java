package com.nttdatabank.credit_service.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Customers")
public class Customer {

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("documentNumber")
    private String documentNumber;
    @Field("type")
    private CustomerType type; // Enum: PERSONAL, BUSINESS

    @Field("documentType")
    private String documentType;

    @Field("status")
    private CustomerStatus status; // Enum: ACTIVE, INACTIVE, SUSPENDED
}