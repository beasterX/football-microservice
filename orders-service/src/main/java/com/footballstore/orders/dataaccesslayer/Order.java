package com.footballstore.orders.dataaccesslayer;

import com.footballstore.orders.domainclientlayer.apparels.ApparelModel;
import com.footballstore.orders.domainclientlayer.customers.CustomerModel;
import com.footballstore.orders.domainclientlayer.warehouses.WarehouseModel;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    private OrderIdentifier orderIdentifier;

    private CustomerModel customerModel;

    private WarehouseModel warehouseModel;

    private List<OrderItem> items;

    private OrderPrice totalPrice;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private LocalDate orderDate;
}
