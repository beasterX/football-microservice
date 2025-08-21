package com.footballstore.apigateway.presentationlayer.orders;

import com.footballstore.apigateway.domainclientlayer.orders.OrderStatus;
import com.footballstore.apigateway.domainclientlayer.orders.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderRequestModel {
    private String warehouseId;
    private List<OrderItemRequestModel> items;

    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
}
