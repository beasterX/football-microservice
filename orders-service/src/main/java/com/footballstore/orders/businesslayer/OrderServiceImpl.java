package com.footballstore.orders.businesslayer;

import com.footballstore.orders.dataaccesslayer.*;
import com.footballstore.orders.domainclientlayer.apparels.ApparelModel;
import com.footballstore.orders.domainclientlayer.apparels.ApparelsServiceClient;
import com.footballstore.orders.domainclientlayer.customers.CustomerModel;
import com.footballstore.orders.domainclientlayer.customers.CustomersServiceClient;
import com.footballstore.orders.domainclientlayer.warehouses.WarehouseModel;
import com.footballstore.orders.domainclientlayer.warehouses.WarehousesServiceClient;
import com.footballstore.orders.mappinglayer.OrderRequestMapper;
import com.footballstore.orders.mappinglayer.OrderResponseMapper;
import com.footballstore.orders.presentationlayer.OrderItemRequestModel;
import com.footballstore.orders.presentationlayer.OrderRequestModel;
import com.footballstore.orders.presentationlayer.OrderResponseModel;
import com.footballstore.orders.utils.exceptions.NotFoundException;
import com.footballstore.orders.utils.exceptions.OrderStateException;
import com.footballstore.orders.utils.exceptions.StockExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomersServiceClient customersClient;
    private final WarehousesServiceClient warehousesClient;
    private final ApparelsServiceClient apparelsClient;
    private final OrderRequestMapper orderEntityMapper;
    private final OrderResponseMapper orderModelMapper;

    @Override
    public List<OrderResponseModel> getAllCustomerOrders(String customerId) {
        return orderRepository
                .findAllByCustomerModel_CustomerId(customerId)
                .stream()
                .map(orderModelMapper::mapToOrderResponse)
                .toList();
    }

    @Override
    public OrderResponseModel getCustomerOrderById(String customerId, String orderId) {
        Order existing = orderRepository
                .findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId, orderId);
        if (existing == null) {
            throw new NotFoundException("Order not found: " + orderId);
        }
        return orderModelMapper.mapToOrderResponse(existing);
    }

    @Override
    public OrderResponseModel processCustomerOrder(String customerId, OrderRequestModel request) {
        CustomerModel cust = customersClient.getCustomerByCustomerId(customerId);
        WarehouseModel wh = warehousesClient.getWarehouseByWarehouseId(request.getWarehouseId());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalAmt = BigDecimal.ZERO;
        String currency = null;

        for (OrderItemRequestModel ri : request.getItems()) {
            ApparelModel app = apparelsClient.getApparelByApparelId(ri.getApparelId());

            // the invariant: you cannot reserve more than exists
            if (apparelsClient.getStock(app.getApparelId()) < ri.getQuantity()) {
                throw new StockExceededException(
                        "Not enough stock for " + app.getApparelId());
            }
            apparelsClient.decreaseStock(app.getApparelId(), ri.getQuantity());

            BigDecimal lineTotal = ri.getUnitPrice()
                    .multiply(BigDecimal.valueOf(ri.getQuantity()))
                    .subtract(ri.getDiscount());

            totalAmt = totalAmt.add(lineTotal);
            currency = Optional.ofNullable(currency).orElse(ri.getCurrency());

            items.add(OrderItem.builder()
                    .orderItemIdentifier(new OrderItemIdentifier())
                    .apparelModel(app)
                    .quantity(ri.getQuantity())
                    .unitPrice(ri.getUnitPrice())
                    .discount(ri.getDiscount())
                    .lineTotal(lineTotal)
                    .build());
        }

        Order order = orderEntityMapper.mapToOrderEntity(
                new OrderIdentifier(),
                cust, wh, request, items,
                new OrderPrice(totalAmt, currency),
                LocalDate.now()
        );
        order.setOrderStatus(OrderStatus.CREATED);
        order.setPaymentStatus(Optional
                .ofNullable(request.getPaymentStatus())
                .orElse(PaymentStatus.PENDING)
        );

        return orderModelMapper
                .mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponseModel updateCustomerOrder(
            String customerId, String orderId, OrderRequestModel request) {

        Order existing = orderRepository
                .findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId, orderId);
        if (existing == null) {
            throw new NotFoundException("Order not found: " + orderId);
        }
        if (existing.getOrderStatus() == OrderStatus.COMPLETED ||
                existing.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new OrderStateException(
                    "Cannot modify order " + orderId + " in state " + existing.getOrderStatus());
        }

        Map<String, Integer> oldQty = existing.getItems().stream()
                .collect(Collectors.toMap(
                        itm -> itm.getApparelModel().getApparelId(),
                        OrderItem::getQuantity));

        Map<String, Integer> newQty = request.getItems().stream()
                .collect(Collectors.toMap(
                        OrderItemRequestModel::getApparelId,
                        OrderItemRequestModel::getQuantity,
                        Integer::sum));

        for (var e : newQty.entrySet()) {
            String apparelId = e.getKey();
            int delta = e.getValue() - oldQty.getOrDefault(apparelId, 0);
            if (delta > 0) {
                if (apparelsClient.getStock(apparelId) < delta) {
                    throw new StockExceededException(
                            "Not enough stock to increase " + apparelId + " by " + delta);
                }
                apparelsClient.decreaseStock(apparelId, delta);
            } else if (delta < 0) {
                apparelsClient.increaseStock(apparelId, -delta);
            }
        }
        for (String removed : oldQty.keySet()) {
            if (!newQty.containsKey(removed)) {
                apparelsClient.increaseStock(removed, oldQty.get(removed));
            }
        }

        List<OrderItem> updatedItems = new ArrayList<>();
        BigDecimal totalAmt = BigDecimal.ZERO;
        String currency = null;

        for (OrderItemRequestModel ri : request.getItems()) {
            ApparelModel app = apparelsClient.getApparelByApparelId(ri.getApparelId());
            BigDecimal lineTotal = ri.getUnitPrice()
                    .multiply(BigDecimal.valueOf(ri.getQuantity()))
                    .subtract(ri.getDiscount());
            totalAmt = totalAmt.add(lineTotal);
            currency = Optional.ofNullable(currency).orElse(ri.getCurrency());

            updatedItems.add(OrderItem.builder()
                    .orderItemIdentifier(new OrderItemIdentifier())
                    .apparelModel(app)
                    .quantity(ri.getQuantity())
                    .unitPrice(ri.getUnitPrice())
                    .discount(ri.getDiscount())
                    .lineTotal(lineTotal)
                    .build());
        }

        existing.setItems(updatedItems);
        existing.setTotalPrice(new OrderPrice(totalAmt, currency));
        existing.setOrderStatus(request.getOrderStatus());
        existing.setPaymentStatus(request.getPaymentStatus());

        return orderModelMapper
                .mapToOrderResponse(orderRepository.save(existing));
    }

    @Override
    public void deleteCustomerOrder(String customerId, String orderId) {
        Order existing = orderRepository
                .findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId, orderId);
        if (existing == null) {
            throw new NotFoundException("Order not found: " + orderId);
        }
        if (existing.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new OrderStateException(
                    "Cannot cancel order " + orderId + " because it is already COMPLETED");
        }

        existing.getItems().forEach(item ->
                apparelsClient.increaseStock(
                        item.getApparelModel().getApparelId(),
                        item.getQuantity()
                )
        );

        existing.setOrderStatus(PaymentStatus.REFUNDED == existing.getPaymentStatus()
                ? OrderStatus.CANCELLED
                : OrderStatus.CANCELLED);
        existing.setPaymentStatus(PaymentStatus.REFUNDED);

        orderRepository.save(existing);
    }
}
