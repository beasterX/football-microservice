package com.footballstore.apigateway.businesslayer.orders;

import com.footballstore.apigateway.presentationlayer.orders.*;

import java.util.List;

public interface OrdersService {
    List<OrderResponseModel> getAllCustomerOrders(String customerId);

    OrderResponseModel getCustomerOrderById(String customerId, String orderId);

    OrderResponseModel processCustomerOrder(String customerId, OrderRequestModel request);

    OrderResponseModel updateCustomerOrder(String customerId, String orderId, OrderRequestModel request);

    void deleteCustomerOrder(String customerId, String orderId);
}
