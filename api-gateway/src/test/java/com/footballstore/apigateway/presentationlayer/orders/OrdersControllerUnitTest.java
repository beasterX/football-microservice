package com.footballstore.apigateway.presentationlayer.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.footballstore.apigateway.businesslayer.orders.OrdersService;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import com.footballstore.apigateway.utils.exceptions.NotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class OrdersControllerUnitTest {

    @Autowired
    private OrdersController controller;

    @MockitoBean
    private OrdersService service;

    private final String CUST_ID  = "c1111111-1111-1111-1111-111111111111";
    private final String ORDER_ID = "o1111111-1111-1111-1111-111111111111";
    private final String BAD_ID   = "bad-id";

    @Test
    void getAllOrders_whenServiceReturnsList_thenOkAndBody() {
        var o = OrderResponseModel.builder()
                .orderId("o1")
                .orderDate(LocalDate.now())
                .customerId("c1")
                .build();

        when(service.getAllCustomerOrders(CUST_ID)).thenReturn(List.of(o));

        ResponseEntity<List<OrderResponseModel>> resp =
                controller.getAllCustomerOrders(CUST_ID);

        assertAll("getAll",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals(1, resp.getBody().size()),
                () -> assertEquals("o1", resp.getBody().get(0).getOrderId())
        );
        verify(service).getAllCustomerOrders(CUST_ID);
    }

    @Test
    void getAllOrders_whenInvalidCustomerId_thenThrows() {
        when(service.getAllCustomerOrders(BAD_ID))
                .thenThrow(new InvalidInputException("Invalid customerId: " + BAD_ID));

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.getAllCustomerOrders(BAD_ID)
        );
        assertTrue(ex.getMessage().contains("Invalid customerId"));
        verify(service).getAllCustomerOrders(BAD_ID);
    }

    @Test
    void getOrderById_whenValid_thenOkAndBody() {
        var o = OrderResponseModel.builder()
                .orderId(ORDER_ID)
                .orderDate(LocalDate.now())
                .customerId(CUST_ID)
                .build();

        when(service.getCustomerOrderById(CUST_ID, ORDER_ID)).thenReturn(o);

        ResponseEntity<OrderResponseModel> resp =
                controller.getCustomerOrderById(CUST_ID, ORDER_ID);

        assertAll("getById",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals(ORDER_ID, resp.getBody().getOrderId())
        );
        verify(service).getCustomerOrderById(CUST_ID, ORDER_ID);
    }

    @Test
    void getOrderById_whenServiceThrowsNotFound_thenPropagates() {
        doThrow(new NotFoundException("nf"))
                .when(service).getCustomerOrderById(CUST_ID, ORDER_ID);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.getCustomerOrderById(CUST_ID, ORDER_ID)
        );
        assertEquals("nf", ex.getMessage());
        verify(service).getCustomerOrderById(CUST_ID, ORDER_ID);
    }

    @Test
    void createOrder_whenValid_thenCreatedAndBody() {
        var req = OrderRequestModel.builder()
                .warehouseId("wh1")
                .items(Collections.emptyList())
                .build();

        var respModel = OrderResponseModel.builder()
                .orderId("oNew")
                .orderDate(LocalDate.now())
                .customerId(CUST_ID)
                .build();

        when(service.processCustomerOrder(CUST_ID, req)).thenReturn(respModel);

        ResponseEntity<OrderResponseModel> resp =
                controller.processCustomerOrder(CUST_ID, req);

        assertAll("create",
                () -> assertEquals(HttpStatus.CREATED, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals("oNew", resp.getBody().getOrderId())
        );
        verify(service).processCustomerOrder(CUST_ID, req);
    }

    @Test
    void createOrder_whenInvalid_thenThrows() {
        var req = OrderRequestModel.builder().build();
        doThrow(new InvalidInputException("bad"))
                .when(service).processCustomerOrder(CUST_ID, req);

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                controller.processCustomerOrder(CUST_ID, req)
        );
        assertEquals("bad", ex.getMessage());
        verify(service).processCustomerOrder(CUST_ID, req);
    }

    @Test
    void updateOrder_whenValid_thenOkAndBody() {
        var req = OrderRequestModel.builder()
                .warehouseId("wh1")
                .items(Collections.emptyList())
                .build();

        var respModel = OrderResponseModel.builder()
                .orderId(ORDER_ID)
                .orderDate(LocalDate.now())
                .customerId(CUST_ID)
                .build();

        when(service.updateCustomerOrder(CUST_ID, ORDER_ID, req)).thenReturn(respModel);

        ResponseEntity<OrderResponseModel> resp =
                controller.updateCustomerOrder(CUST_ID, ORDER_ID, req);

        assertAll("update",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals(ORDER_ID, resp.getBody().getOrderId())
        );
        verify(service).updateCustomerOrder(CUST_ID, ORDER_ID, req);
    }

    @Test
    void updateOrder_whenServiceThrowsNotFound_thenPropagates() {
        var req = OrderRequestModel.builder().build();
        doThrow(new NotFoundException("nf"))
                .when(service).updateCustomerOrder(CUST_ID, ORDER_ID, req);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.updateCustomerOrder(CUST_ID, ORDER_ID, req)
        );
        assertEquals("nf", ex.getMessage());
        verify(service).updateCustomerOrder(CUST_ID, ORDER_ID, req);
    }

    @Test
    void deleteOrder_whenValid_thenNoContent() {
        doNothing().when(service).deleteCustomerOrder(CUST_ID, ORDER_ID);

        ResponseEntity<Void> resp =
                controller.deleteCustomerOrder(CUST_ID, ORDER_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(service).deleteCustomerOrder(CUST_ID, ORDER_ID);
    }

    @Test
    void deleteOrder_whenServiceThrowsNotFound_thenPropagates() {
        doThrow(new NotFoundException("nf"))
                .when(service).deleteCustomerOrder(CUST_ID, ORDER_ID);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                controller.deleteCustomerOrder(CUST_ID, ORDER_ID)
        );
        assertEquals("nf", ex.getMessage());
        verify(service).deleteCustomerOrder(CUST_ID, ORDER_ID);
    }
}
