package com.footballstore.orders.businesslayer;

import com.footballstore.orders.dataaccesslayer.*;
import com.footballstore.orders.domainclientlayer.apparels.*;
import com.footballstore.orders.domainclientlayer.customers.*;
import com.footballstore.orders.domainclientlayer.warehouses.*;
import com.footballstore.orders.mappinglayer.OrderRequestMapper;
import com.footballstore.orders.mappinglayer.OrderResponseMapper;
import com.footballstore.orders.presentationlayer.OrderItemRequestModel;
import com.footballstore.orders.presentationlayer.OrderRequestModel;
import com.footballstore.orders.presentationlayer.OrderResponseModel;
import com.footballstore.orders.utils.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration")
@ActiveProfiles("test")
class OrderServiceUnitTest {

    @Autowired
    private OrderServiceImpl orderService;

    @MockitoBean private OrderRepository orderRepository;
    @MockitoBean private CustomersServiceClient customersClient;
    @MockitoBean private WarehousesServiceClient warehousesClient;
    @MockitoBean private ApparelsServiceClient apparelsClient;
    @MockitoSpyBean private OrderRequestMapper orderRequestMapper;
    @MockitoSpyBean private OrderResponseMapper orderResponseMapper;

    private String customerId;
    private String warehouseId;
    private String apparelId;

    @BeforeEach
    void setUp() {
        customerId  = UUID.randomUUID().toString();
        warehouseId = UUID.randomUUID().toString();
        apparelId   = UUID.randomUUID().toString();
        reset(orderRepository, customersClient, warehousesClient, apparelsClient, orderResponseMapper);
    }

    //  processCustomerOrder tests

    @Test
    void processCustomerOrder_validInput_returnsOrderResponse() {
        var itemReq = OrderItemRequestModel.builder()
                .apparelId(apparelId).quantity(2)
                .unitPrice(new BigDecimal("10.00"))
                .discount(BigDecimal.ZERO).currency("USD")
                .build();
        var req = OrderRequestModel.builder()
                .warehouseId(warehouseId)
                .items(List.of(itemReq))
                .build();

        var cm = CustomerModel.builder()
                .customerId(customerId).firstName("Jane").lastName("Doe")
                .email("jane@doe.com").phone("555-0000")
                .registrationDate(LocalDate.now())
                .preferredContact(ContactMethod.EMAIL)
                .address(new Address("Street","City","ST","Country","00000"))
                .build();
        when(customersClient.getCustomerByCustomerId(customerId)).thenReturn(cm);

        var wm = WarehouseModel.builder()
                .warehouseId(warehouseId).locationName("Main").address("Addr").capacity(10)
                .build();
        when(warehousesClient.getWarehouseByWarehouseId(warehouseId)).thenReturn(wm);

        var am = ApparelModel.builder()
                .apparelId(apparelId).itemName("Shirt").description("Desc")
                .brand("Brand").price(new BigDecimal("10.00"))
                .cost(new BigDecimal("5.00")).stock(5)
                .apparelType(ApparelType.JERSEY).sizeOption(SizeOption.M)
                .build();
        when(apparelsClient.getApparelByApparelId(apparelId)).thenReturn(am);
        when(apparelsClient.getStock(apparelId)).thenReturn(5);
        doNothing().when(apparelsClient).decreaseStock(apparelId, 2);

        var savedOrder = Order.builder()
                .orderIdentifier(new OrderIdentifier("fixed-order-id"))
                .customerModel(cm)
                .warehouseModel(wm)
                .items(List.of())
                .totalPrice(new OrderPrice(new BigDecimal("20.00"), "USD"))
                .orderDate(LocalDate.now())
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        var expectedResponse = new OrderResponseModel();
        expectedResponse.setOrderId("fixed-order-id");
        expectedResponse.setOrderStatus(OrderStatus.CREATED);
        expectedResponse.setPaymentStatus(PaymentStatus.PENDING);

        doReturn(expectedResponse)
                .when(orderResponseMapper).mapToOrderResponse(savedOrder);

        // act
        OrderResponseModel resp = orderService.processCustomerOrder(customerId, req);

        // assert
        assertNotNull(resp);
        assertEquals("fixed-order-id", resp.getOrderId());
        assertEquals(OrderStatus.CREATED, resp.getOrderStatus());
        assertEquals(PaymentStatus.PENDING, resp.getPaymentStatus());
        verify(apparelsClient).decreaseStock(apparelId, 2);
    }


    @Test
    void processCustomerOrder_insufficientStock_throwsStockExceededException() {
        var itemReq = OrderItemRequestModel.builder()
                .apparelId(apparelId).quantity(10)
                .unitPrice(new BigDecimal("1.00"))
                .discount(BigDecimal.ZERO).currency("USD")
                .build();
        var req = OrderRequestModel.builder()
                .warehouseId(warehouseId)
                .items(List.of(itemReq))
                .build();

        when(customersClient.getCustomerByCustomerId(anyString()))
                .thenReturn(CustomerModel.builder().customerId(customerId).build());
        when(warehousesClient.getWarehouseByWarehouseId(anyString()))
                .thenReturn(WarehouseModel.builder().warehouseId(warehouseId).build());
        when(apparelsClient.getApparelByApparelId(anyString()))
                .thenReturn(ApparelModel.builder().apparelId(apparelId).build());
        when(apparelsClient.getStock(apparelId)).thenReturn(5);

        var ex = assertThrows(StockExceededException.class,
                () -> orderService.processCustomerOrder(customerId, req));
        assertTrue(ex.getMessage().contains(apparelId));
    }

    @Test
    void processCustomerOrder_customerServiceNotFound_throwsNotFoundException() {
        var req = OrderRequestModel.builder().warehouseId(warehouseId).items(List.of()).build();
        when(customersClient.getCustomerByCustomerId(customerId))
                .thenThrow(new NotFoundException("no such customer"));

        var ex = assertThrows(NotFoundException.class,
                () -> orderService.processCustomerOrder(customerId, req));
        assertEquals("no such customer", ex.getMessage());
    }

    @Test
    void processCustomerOrder_warehouseServiceNotFound_throwsNotFoundException() {
        when(customersClient.getCustomerByCustomerId(customerId))
                .thenReturn(CustomerModel.builder().customerId(customerId).build());
        when(warehousesClient.getWarehouseByWarehouseId(warehouseId))
                .thenThrow(new NotFoundException("no such warehouse"));

        var req = OrderRequestModel.builder().warehouseId(warehouseId).items(List.of()).build();

        var ex = assertThrows(NotFoundException.class,
                () -> orderService.processCustomerOrder(customerId, req));
        assertEquals("no such warehouse", ex.getMessage());
    }

    //  getAllCustomerOrders tests
    @Test
    void getAllCustomerOrders_emptyList_returnsEmpty() {
        when(orderRepository.findAllByCustomerModel_CustomerId(customerId))
                .thenReturn(List.of());
        List<OrderResponseModel> result = orderService.getAllCustomerOrders(customerId);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllCustomerOrders_nonEmptyList_returnsMappedList() {
        var ord1 = new Order();
        var resp1 = new OrderResponseModel();
        resp1.setOrderId("id1");

        when(orderRepository.findAllByCustomerModel_CustomerId(customerId))
                .thenReturn(List.of(ord1));
        doReturn(resp1).when(orderResponseMapper).mapToOrderResponse(ord1);

        List<OrderResponseModel> result = orderService.getAllCustomerOrders(customerId);
        assertEquals(1, result.size());
        assertEquals("id1", result.get(0).getOrderId());
    }

    //  getCustomerOrderById tests

    @Test
    void getCustomerOrderById_orderNotFound_throwsNotFoundException() {
        when(orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId, "nope"))
                .thenReturn(null);
        assertThrows(NotFoundException.class,
                () -> orderService.getCustomerOrderById(customerId, "nope"));
    }

    @Test
    void getCustomerOrderById_orderExists_returnsMappedResponse() {
        var ord = new Order();
        var resp = new OrderResponseModel();
        resp.setOrderId("the-id");
        when(orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId, "the-id"))
                .thenReturn(ord);
        doReturn(resp).when(orderResponseMapper).mapToOrderResponse(ord);

        OrderResponseModel result = orderService.getCustomerOrderById(customerId, "the-id");
        assertEquals("the-id", result.getOrderId());
    }


    @Test
    void updateCustomerOrder_orderCompleted_throwsOrderStateException() {
        var existing = Order.builder()
                .orderIdentifier(new OrderIdentifier("oX"))
                .customerModel(CustomerModel.builder().customerId(customerId).build())
                .orderStatus(OrderStatus.COMPLETED)
                .items(List.of())
                .totalPrice(new OrderPrice(BigDecimal.ZERO,"USD"))
                .orderDate(LocalDate.now())
                .build();
        when(orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId,"oX"))
                .thenReturn(existing);

        assertThrows(OrderStateException.class,
                () -> orderService.updateCustomerOrder(customerId,"oX",OrderRequestModel.builder().warehouseId("w").items(List.of()).build()));
    }

    @Test
    void updateCustomerOrder_increaseQuantity_callsDecreaseStock() {
        var itemModel = ApparelModel.builder().apparelId("A").build();
        var existingItem = OrderItem.builder()
                .orderItemIdentifier(new OrderItemIdentifier())
                .apparelModel(itemModel)
                .quantity(1).unitPrice(BigDecimal.ZERO).discount(BigDecimal.ZERO).lineTotal(BigDecimal.ZERO)
                .build();
        var existing = Order.builder()
                .orderIdentifier(new OrderIdentifier("o2"))
                .customerModel(CustomerModel.builder().customerId(customerId).build())
                .warehouseModel(WarehouseModel.builder().warehouseId("w").build())
                .items(List.of(existingItem))
                .totalPrice(new OrderPrice(BigDecimal.ZERO,"USD"))
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .orderDate(LocalDate.now())
                .build();
        when(orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId,"o2"))
                .thenReturn(existing);
        when(apparelsClient.getStock("A")).thenReturn(5);
        doNothing().when(apparelsClient).decreaseStock("A",2);

        var reqItem = OrderItemRequestModel.builder().apparelId("A").quantity(3)
                .unitPrice(BigDecimal.ZERO).discount(BigDecimal.ZERO).currency("USD").build();
        var req = OrderRequestModel.builder().warehouseId("w").items(List.of(reqItem))
                .orderStatus(OrderStatus.PROCESSING).paymentStatus(PaymentStatus.AUTHORIZED).build();

        when(orderRepository.save(any(Order.class))).thenReturn(existing);
        doReturn(new OrderResponseModel()).when(orderResponseMapper).mapToOrderResponse(existing);

        orderService.updateCustomerOrder(customerId,"o2",req);
        verify(apparelsClient).decreaseStock("A",2);
    }

    @Test
    void updateCustomerOrder_decreaseQuantity_callsIncreaseStock() {
        var itemModel = ApparelModel.builder().apparelId("B").build();
        var existingItem = OrderItem.builder()
                .orderItemIdentifier(new OrderItemIdentifier())
                .apparelModel(itemModel)
                .quantity(5).unitPrice(BigDecimal.ZERO).discount(BigDecimal.ZERO).lineTotal(BigDecimal.ZERO)
                .build();
        var existing = Order.builder()
                .orderIdentifier(new OrderIdentifier("o3"))
                .customerModel(CustomerModel.builder().customerId(customerId).build())
                .warehouseModel(WarehouseModel.builder().warehouseId("w").build())
                .items(List.of(existingItem))
                .totalPrice(new OrderPrice(BigDecimal.ZERO,"USD"))
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .orderDate(LocalDate.now())
                .build();
        when(orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId,"o3"))
                .thenReturn(existing);
        doNothing().when(apparelsClient).increaseStock("B",3);

        var reqItem = OrderItemRequestModel.builder().apparelId("B").quantity(2)
                .unitPrice(BigDecimal.ZERO).discount(BigDecimal.ZERO).currency("USD").build();
        var req = OrderRequestModel.builder().warehouseId("w").items(List.of(reqItem))
                .orderStatus(OrderStatus.PROCESSING).paymentStatus(PaymentStatus.AUTHORIZED).build();

        when(orderRepository.save(any(Order.class))).thenReturn(existing);
        doReturn(new OrderResponseModel()).when(orderResponseMapper).mapToOrderResponse(existing);

        orderService.updateCustomerOrder(customerId,"o3",req);
        verify(apparelsClient).increaseStock("B",3);
    }


    //  deleteCustomerOrder tests

    @Test
    void deleteCustomerOrder_notFound_throwsNotFoundException() {
        when(orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(anyString(),anyString()))
                .thenReturn(null);
        assertThrows(NotFoundException.class,
                () -> orderService.deleteCustomerOrder(customerId,"x"));
    }

    @Test
    void deleteCustomerOrder_completedOrder_throwsOrderStateException() {
        var existing = Order.builder()
                .orderIdentifier(new OrderIdentifier("z"))
                .customerModel(CustomerModel.builder().customerId(customerId).build())
                .orderStatus(OrderStatus.COMPLETED)
                .paymentStatus(PaymentStatus.CAPTURED)
                .items(List.of())
                .totalPrice(new OrderPrice(BigDecimal.ZERO,"USD"))
                .orderDate(LocalDate.now())
                .build();
        when(orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId,"z"))
                .thenReturn(existing);

        assertThrows(OrderStateException.class,
                () -> orderService.deleteCustomerOrder(customerId,"z"));
    }

    @Test
    void deleteCustomerOrder_withNoItems_savesCancelledOrder() {
        var existing = Order.builder()
                .orderIdentifier(new OrderIdentifier("y"))
                .customerModel(CustomerModel.builder().customerId(customerId).build())
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .items(List.of())
                .totalPrice(new OrderPrice(BigDecimal.ZERO,"USD"))
                .orderDate(LocalDate.now())
                .build();
        when(orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId,"y"))
                .thenReturn(existing);
        when(orderRepository.save(any(Order.class))).thenReturn(existing);

        orderService.deleteCustomerOrder(customerId,"y");
        verify(orderRepository).save(existing);
        verify(apparelsClient, never()).increaseStock(anyString(), anyInt());
    }

    @Test
    void deleteCustomerOrder_withItems_restocksAndSaves() {
        var itemModel = ApparelModel.builder().apparelId("C").build();
        var orderItem = OrderItem.builder()
                .orderItemIdentifier(new OrderItemIdentifier())
                .apparelModel(itemModel)
                .quantity(4).unitPrice(BigDecimal.ZERO).discount(BigDecimal.ZERO).lineTotal(BigDecimal.ZERO)
                .build();
        var existing = Order.builder()
                .orderIdentifier(new OrderIdentifier("x"))
                .customerModel(CustomerModel.builder().customerId(customerId).build())
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .items(List.of(orderItem))
                .totalPrice(new OrderPrice(BigDecimal.ZERO,"USD"))
                .orderDate(LocalDate.now())
                .build();

        when(orderRepository.findByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(customerId,"x"))
                .thenReturn(existing);
        doNothing().when(apparelsClient).increaseStock("C",4);
        when(orderRepository.save(any(Order.class))).thenReturn(existing);

        orderService.deleteCustomerOrder(customerId,"x");
        verify(apparelsClient).increaseStock("C",4);
        verify(orderRepository).save(existing);
    }
}
