package com.footballstore.apigateway.presentationlayer.orders;

import com.footballstore.apigateway.domainclientlayer.orders.OrderStatus;
import com.footballstore.apigateway.domainclientlayer.orders.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class OrderResponseModel extends RepresentationModel<OrderResponseModel> {
    private String orderId;

    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    private LocalDate orderDate;

    private String warehouseId;
    private String locationName;
    private String warehouseAddress;
    private Integer capacity;

    private BigDecimal totalAmount;
    private String currency;

    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;

    private List<OrderItemResponseModel> items;
}
