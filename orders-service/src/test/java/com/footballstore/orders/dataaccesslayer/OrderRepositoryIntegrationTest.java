package com.footballstore.orders.dataaccesslayer;

import com.footballstore.orders.domainclientlayer.customers.CustomerModel;
import com.footballstore.orders.domainclientlayer.warehouses.WarehouseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@ActiveProfiles("test")
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanAndSeed() {
        orderRepository.deleteAll();
        var order1 = Order.builder()
                .orderIdentifier(new OrderIdentifier("oid1"))
                .customerModel(CustomerModel.builder().customerId("cust1").build())
                .warehouseModel(WarehouseModel.builder().warehouseId("w1").build())
                .items(List.of())
                .totalPrice(new OrderPrice(new BigDecimal("50.00"), "USD"))
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .orderDate(LocalDate.now())
                .build();
        var order2 = Order.builder()
                .orderIdentifier(new OrderIdentifier("oid2"))
                .customerModel(CustomerModel.builder().customerId("cust2").build())
                .warehouseModel(WarehouseModel.builder().warehouseId("w2").build())
                .items(List.of())
                .totalPrice(new OrderPrice(new BigDecimal("75.00"), "USD"))
                .orderStatus(OrderStatus.PROCESSING)
                .paymentStatus(PaymentStatus.AUTHORIZED)
                .orderDate(LocalDate.now())
                .build();
        orderRepository.save(order1);
        orderRepository.save(order2);
    }

    @Test
    void whenFindAllByCustomerModel_CustomerIdExists_thenReturnsMatchingOrders() {
        List<Order> list = orderRepository.findAllByCustomerModel_CustomerId("cust1");
        assertEquals(1, list.size());
        assertEquals("oid1", list.get(0).getOrderIdentifier().getOrderId());
    }

    @Test
    void whenFindAllByCustomerModel_CustomerIdNotExists_thenReturnsEmptyList() {
        List<Order> list = orderRepository.findAllByCustomerModel_CustomerId("nope");
        assertTrue(list.isEmpty());
    }

    @Test
    void whenFindByCustomerAndOrderIdExists_thenReturnsOrder() {
        Order o = orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId("cust2", "oid2");
        assertNotNull(o);
        assertEquals(OrderStatus.PROCESSING, o.getOrderStatus());
    }

    @Test
    void whenFindByCustomerAndOrderIdNotExists_thenReturnsNull() {
        Order o = orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId("cust1","wrong");
        assertNull(o);
    }

    @Test
    void whenSaveNewOrder_thenCanRetrieveIt() {
        var newOrder = Order.builder()
                .orderIdentifier(new OrderIdentifier("oid3"))
                .customerModel(CustomerModel.builder().customerId("cust3").build())
                .warehouseModel(WarehouseModel.builder().warehouseId("w3").build())
                .items(List.of())
                .totalPrice(new OrderPrice(new BigDecimal("100.00"), "USD"))
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .orderDate(LocalDate.now())
                .build();
        orderRepository.save(newOrder);

        Order fetched = orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId("cust3","oid3");
        assertNotNull(fetched);
        assertEquals("oid3", fetched.getOrderIdentifier().getOrderId());
    }
}
