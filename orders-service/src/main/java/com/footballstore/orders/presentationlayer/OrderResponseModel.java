package com.footballstore.orders.presentationlayer;

import com.footballstore.orders.dataaccesslayer.OrderStatus;
import com.footballstore.orders.dataaccesslayer.PaymentStatus;
import com.footballstore.orders.domainclientlayer.customers.Address;
import com.footballstore.orders.domainclientlayer.customers.ContactMethod;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseModel {
    private String orderId;
    private LocalDate orderDate;
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate registrationDate;
    private ContactMethod preferredContact;
    private Address address;
    private String warehouseId;
    private String locationName;
    private String warehouseAddress;
    private Integer capacity;
    private List<OrderItemResponseModel> items;
    private BigDecimal totalAmount;
    private String currency;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
}
