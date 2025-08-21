package com.footballstore.orders.presentationlayer;

import com.footballstore.orders.businesslayer.OrderService;
import com.footballstore.orders.dataaccesslayer.OrderStatus;
import com.footballstore.orders.dataaccesslayer.PaymentStatus;
import com.footballstore.orders.presentationlayer.OrderController;
import com.footballstore.orders.presentationlayer.OrderItemRequestModel;
import com.footballstore.orders.presentationlayer.OrderRequestModel;
import com.footballstore.orders.presentationlayer.OrderResponseModel;
import com.footballstore.orders.utils.exceptions.InvalidInputException;
import com.footballstore.orders.utils.exceptions.NotFoundException;
import com.footballstore.orders.utils.exceptions.OrderStateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = OrderController.class,
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
        }
)
@ActiveProfiles("test")
class OrderControllerUnitTest {

    private static final String VALID_CUSTOMER = "00000000-0000-0000-0000-000000000000";
    private static final String VALID_ORDER    = "11111111-1111-1111-1111-111111111111";

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private OrderController orderController;

    @Test
    void getAllCustomerOrders_validCustomer_returnsEmptyList() {
        when(orderService.getAllCustomerOrders(VALID_CUSTOMER))
                .thenReturn(Collections.emptyList());

        List<OrderResponseModel> resp =
                orderController.getAllCustomerOrders(VALID_CUSTOMER);

        assertNotNull(resp);
        assertTrue(resp.isEmpty());
        verify(orderService).getAllCustomerOrders(VALID_CUSTOMER);
    }

    @Test
    void getAllCustomerOrders_validCustomer_returnsNonEmptyList() {
        var m = new OrderResponseModel();
        m.setOrderId("foo");
        when(orderService.getAllCustomerOrders(VALID_CUSTOMER))
                .thenReturn(List.of(m));

        List<OrderResponseModel> resp =
                orderController.getAllCustomerOrders(VALID_CUSTOMER);

        assertEquals(1, resp.size());
        assertEquals("foo", resp.get(0).getOrderId());
    }

    @Test
    void getAllCustomerOrders_invalidCustomer_throwsInvalidInput() {
        String bad = "too-short";
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> orderController.getAllCustomerOrders(bad)
        );
        assertEquals("Invalid customerId: " + bad, ex.getMessage());
        verify(orderService, never()).getAllCustomerOrders(any());
    }

    @Test
    void getCustomerOrderById_validIds_returnsOk() {
        var dummy = new OrderResponseModel();
        dummy.setOrderId(VALID_ORDER);
        when(orderService.getCustomerOrderById(VALID_CUSTOMER, VALID_ORDER))
                .thenReturn(dummy);

        ResponseEntity<OrderResponseModel> resp =
                orderController.getCustomerOrderById(VALID_CUSTOMER, VALID_ORDER);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(dummy, resp.getBody());
        verify(orderService).getCustomerOrderById(VALID_CUSTOMER, VALID_ORDER);
    }

    @Test
    void getCustomerOrderById_invalidCustomer_throwsInvalidInput() {
        assertThrows(
                InvalidInputException.class,
                () -> orderController.getCustomerOrderById("short", VALID_ORDER)
        );
        verify(orderService, never()).getCustomerOrderById(any(), any());
    }

    @Test
    void getCustomerOrderById_invalidOrder_throwsInvalidInput() {
        assertThrows(
                InvalidInputException.class,
                () -> orderController.getCustomerOrderById(VALID_CUSTOMER, "short")
        );
        verify(orderService, never()).getCustomerOrderById(any(), any());
    }

    @Test
    void getCustomerOrderById_serviceThrowsNotFound_propagates() {
        when(orderService.getCustomerOrderById(VALID_CUSTOMER, VALID_ORDER))
                .thenThrow(new NotFoundException("no such order"));

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> orderController.getCustomerOrderById(VALID_CUSTOMER, VALID_ORDER)
        );
        assertEquals("no such order", ex.getMessage());
    }

    @Test
    void processCustomerOrder_validInput_returnsCreated() {
        var req = OrderRequestModel.builder()
                .warehouseId(UUID.randomUUID().toString())
                .items(List.of(
                        OrderItemRequestModel.builder()
                                .apparelId("A")
                                .quantity(1)
                                .unitPrice(new BigDecimal("9.99"))
                                .discount(BigDecimal.ZERO)
                                .currency("USD")
                                .build()
                )).build();

        var created = new OrderResponseModel();
        created.setOrderId("xyz");
        when(orderService.processCustomerOrder(VALID_CUSTOMER, req))
                .thenReturn(created);

        ResponseEntity<OrderResponseModel> resp =
                orderController.processCustomerOrder(VALID_CUSTOMER, req);

        assertEquals(201, resp.getStatusCodeValue());
        assertSame(created, resp.getBody());
    }

    @Test
    void processCustomerOrder_serviceThrowsNotFound_propagates() {
        var req = OrderRequestModel.builder()
                .warehouseId("w")
                .items(Collections.emptyList())
                .build();
        when(orderService.processCustomerOrder(VALID_CUSTOMER, req))
                .thenThrow(new NotFoundException("cust missing"));

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> orderController.processCustomerOrder(VALID_CUSTOMER, req)
        );
        assertEquals("cust missing", ex.getMessage());
    }

    @Test
    void processCustomerOrder_serviceThrowsInvalidInput_propagates() {
        var req = OrderRequestModel.builder()
                .warehouseId("w")
                .items(Collections.emptyList())
                .build();
        when(orderService.processCustomerOrder(VALID_CUSTOMER, req))
                .thenThrow(new InvalidInputException("bad data"));

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> orderController.processCustomerOrder(VALID_CUSTOMER, req)
        );
        assertEquals("bad data", ex.getMessage());
    }

    @Test
    void updateCustomerOrder_validInput_returnsOk() {
        var req = OrderRequestModel.builder()
                .warehouseId("w")
                .items(Collections.emptyList())
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        var updated = new OrderResponseModel();
        updated.setOrderId(VALID_ORDER);

        when(orderService.updateCustomerOrder(VALID_CUSTOMER, VALID_ORDER, req))
                .thenReturn(updated);

        ResponseEntity<OrderResponseModel> resp =
                orderController.updateCustomerOrder(VALID_CUSTOMER, VALID_ORDER, req);

        assertEquals(200, resp.getStatusCodeValue());
        assertSame(updated, resp.getBody());
    }

    @Test
    void updateCustomerOrder_serviceThrowsNotFound_propagates() {
        var req = OrderRequestModel.builder()
                .warehouseId("w")
                .items(Collections.emptyList())
                .build();
        when(orderService.updateCustomerOrder(VALID_CUSTOMER, VALID_ORDER, req))
                .thenThrow(new NotFoundException("not found"));

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> orderController.updateCustomerOrder(VALID_CUSTOMER, VALID_ORDER, req)
        );
        assertEquals("not found", ex.getMessage());
    }

    @Test
    void updateCustomerOrder_serviceThrowsOrderState_propagates() {
        var req = OrderRequestModel.builder()
                .warehouseId("w")
                .items(Collections.emptyList())
                .build();
        when(orderService.updateCustomerOrder(VALID_CUSTOMER, VALID_ORDER, req))
                .thenThrow(new OrderStateException("bad state"));

        OrderStateException ex = assertThrows(
                OrderStateException.class,
                () -> orderController.updateCustomerOrder(VALID_CUSTOMER, VALID_ORDER, req)
        );
        assertEquals("bad state", ex.getMessage());
    }

    @Test
    void deleteCustomerOrder_validIds_returnsNoContent() {
        doNothing().when(orderService).deleteCustomerOrder(VALID_CUSTOMER, VALID_ORDER);

        ResponseEntity<Void> resp =
                orderController.deleteCustomerOrder(VALID_CUSTOMER, VALID_ORDER);

        assertEquals(204, resp.getStatusCodeValue());
        verify(orderService).deleteCustomerOrder(VALID_CUSTOMER, VALID_ORDER);
    }

    @Test
    void deleteCustomerOrder_invalidCustomer_throwsInvalidInput() {
        String bad = "too-short";
        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> orderController.deleteCustomerOrder(bad, VALID_ORDER)
        );
        assertEquals("Invalid customerId: " + bad, ex.getMessage());
        verify(orderService, never()).deleteCustomerOrder(any(), any());
    }

    @Test
    void deleteCustomerOrder_serviceThrowsNotFound_propagates() {
        doThrow(new NotFoundException("gone"))
                .when(orderService).deleteCustomerOrder(VALID_CUSTOMER, VALID_ORDER);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> orderController.deleteCustomerOrder(VALID_CUSTOMER, VALID_ORDER)
        );
        assertEquals("gone", ex.getMessage());
    }

    @Test
    void deleteCustomerOrder_serviceThrowsOrderState_propagates() {
        doThrow(new OrderStateException("cannot cancel"))
                .when(orderService).deleteCustomerOrder(VALID_CUSTOMER, VALID_ORDER);

        OrderStateException ex = assertThrows(
                OrderStateException.class,
                () -> orderController.deleteCustomerOrder(VALID_CUSTOMER, VALID_ORDER)
        );
        assertEquals("cannot cancel", ex.getMessage());
    }
}
