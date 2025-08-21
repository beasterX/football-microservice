package com.footballstore.apigateway.businesslayer.orders;

import com.footballstore.apigateway.domainclientlayer.customers.CustomersServiceClient;
import com.footballstore.apigateway.domainclientlayer.warehouses.WarehousesServiceClient;
import com.footballstore.apigateway.domainclientlayer.orders.OrdersServiceClient;
import com.footballstore.apigateway.presentationlayer.orders.OrderResponseModel;
import com.footballstore.apigateway.presentationlayer.orders.OrderRequestModel;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@Service
public class OrdersServiceImpl implements OrdersService {

    private static final int UUID_LEN = 36;

    private final OrdersServiceClient ordersClient;
    private final CustomersServiceClient customersClient;
    private final WarehousesServiceClient warehousesClient;

    public OrdersServiceImpl(OrdersServiceClient ordersClient,
                             CustomersServiceClient customersClient,
                             WarehousesServiceClient warehousesClient) {
        this.ordersClient = ordersClient;
        this.customersClient = customersClient;
        this.warehousesClient = warehousesClient;
    }

    @Override
    public List<OrderResponseModel> getAllCustomerOrders(String customerId) {
        validateUuid(customerId, "customerId");
        List<OrderResponseModel> orders = ordersClient.getAllCustomerOrders(customerId);
        orders.forEach(this::enrichOrder);
        return orders;
    }

    @Override
    public OrderResponseModel getCustomerOrderById(String customerId, String orderId) {
        validateUuid(customerId, "customerId");
        validateUuid(orderId, "orderId");
        OrderResponseModel order = ordersClient.getCustomerOrderById(customerId, orderId);
        enrichOrder(order);
        return order;
    }

    @Override
    public OrderResponseModel processCustomerOrder(String customerId, OrderRequestModel request) {
        validateUuid(customerId, "customerId");
        OrderResponseModel created = ordersClient.processCustomerOrder(customerId, request);
        enrichOrder(created);
        return created;
    }

    @Override
    public OrderResponseModel updateCustomerOrder(String customerId, String orderId, OrderRequestModel request) {
        validateUuid(customerId, "customerId");
        validateUuid(orderId, "orderId");
        OrderResponseModel updated = ordersClient.updateCustomerOrder(customerId, orderId, request);
        enrichOrder(updated);
        return updated;
    }

    @Override
    public void deleteCustomerOrder(String customerId, String orderId) {
        validateUuid(customerId, "customerId");
        validateUuid(orderId, "orderId");
        ordersClient.deleteCustomerOrder(customerId, orderId);
    }

    private void enrichOrder(OrderResponseModel order) {
        if (order == null || order.getOrderId() == null) return;

        var c = customersClient.getCustomerById(order.getCustomerId());
        order.setFirstName(c.getFirstName());
        order.setLastName(c.getLastName());
        order.setEmail(c.getEmail());
        order.setPhone(c.getPhone());

        var w = warehousesClient.getWarehouseById(order.getWarehouseId());
        order.setLocationName(w.getLocationName());
        order.setWarehouseAddress(w.getAddress());
        order.setCapacity(w.getCapacity());

        order.add(linkTo(methodOn(com.footballstore.apigateway.presentationlayer.orders.OrdersController.class)
                .getCustomerOrderById(order.getCustomerId(), order.getOrderId()))
                .withSelfRel());
        order.add(linkTo(methodOn(com.footballstore.apigateway.presentationlayer.orders.OrdersController.class)
                .getAllCustomerOrders(order.getCustomerId()))
                .withRel("all-orders"));
    }

    private void validateUuid(String id, String name) {
        try {
            if (id.length() != UUID_LEN) throw new IllegalArgumentException();
            UUID.fromString(id);
        } catch (Exception ex) {
            throw new InvalidInputException("Invalid " + name + ": " + id);
        }
    }
}
