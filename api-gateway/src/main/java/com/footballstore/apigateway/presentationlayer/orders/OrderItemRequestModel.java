package com.footballstore.apigateway.presentationlayer.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
