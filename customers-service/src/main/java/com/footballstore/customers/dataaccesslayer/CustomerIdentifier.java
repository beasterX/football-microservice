package com.footballstore.customers.dataaccesslayer;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.UUID;

@Embeddable
@Data
public class CustomerIdentifier {

    @Column(name = "CUSTOMER_ID")
    private String customerId;

    protected CustomerIdentifier() {
    }

    public CustomerIdentifier(String customerId) {
        this.customerId = customerId;
    }

    public static CustomerIdentifier generate() {
        return new CustomerIdentifier(UUID.randomUUID().toString());
    }
}
