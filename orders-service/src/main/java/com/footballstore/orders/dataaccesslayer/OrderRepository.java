package com.footballstore.orders.dataaccesslayer;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findAllByCustomerModel_CustomerId(String customerId);

    Order findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(String customerId, String orderId);
}
