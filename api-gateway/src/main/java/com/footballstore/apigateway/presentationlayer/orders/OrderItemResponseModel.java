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
public class OrderItemResponseModel {
    private String orderItemId;
    private String apparelId;
    private String itemName;
    private String description;
    private String brand;
    private BigDecimal unitPrice;
    private BigDecimal cost;
    private Integer quantity;
    private BigDecimal discount;
    private BigDecimal lineTotal;
    private String apparelType;
    private String sizeOption;
}
