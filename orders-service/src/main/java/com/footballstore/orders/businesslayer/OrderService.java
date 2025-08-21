package com.footballstore.orders.businesslayer;

import com.footballstore.orders.presentationlayer.OrderRequestModel;
import com.footballstore.orders.presentationlayer.OrderResponseModel;

import java.util.List;

public interface OrderService {

    List<OrderResponseModel> getAllCustomerOrders(String customerId);

    OrderResponseModel getCustomerOrderById(String customerId, String orderId);

    OrderResponseModel processCustomerOrder(String customerId, OrderRequestModel request);

    OrderResponseModel updateCustomerOrder(String customerId, String orderId, OrderRequestModel request);

    void deleteCustomerOrder(String customerId, String orderId);
}
