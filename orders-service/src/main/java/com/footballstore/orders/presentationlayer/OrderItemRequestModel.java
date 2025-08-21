package com.footballstore.orders.presentationlayer;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequestModel {
    private String apparelId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private String currency;
}
