package com.footballstore.orders.dataaccesslayer;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
public class OrderPrice {

    private BigDecimal amount;

    private String currency;

    public OrderPrice(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }
}
