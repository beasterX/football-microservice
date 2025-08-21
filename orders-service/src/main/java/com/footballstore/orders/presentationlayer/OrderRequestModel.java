package com.footballstore.orders.presentationlayer;

import com.footballstore.orders.dataaccesslayer.OrderStatus;
import com.footballstore.orders.dataaccesslayer.PaymentStatus;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestModel {
    private String warehouseId;
    private List<OrderItemRequestModel> items;
    private OrderStatus  orderStatus;
    private PaymentStatus paymentStatus;
}
