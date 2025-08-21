package com.footballstore.apigateway.presentationlayer.orders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import com.footballstore.apigateway.domainclientlayer.orders.OrderStatus;
import com.footballstore.apigateway.domainclientlayer.orders.PaymentStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.orders-service.host=localhost",
        "app.orders-service.port=7004",
        "app.customers-service.host=localhost",
        "app.customers-service.port=7001",
        "app.warehouses-service.host=localhost",
        "app.warehouses-service.port=7002"
})
class OrdersControllerIntegrationTest {

    private static final String GATEWAY_BASE = "/api/v1/customers";
    private static final String ORDERS_PATH = "/orders";
    private static final String ORDERS_BASE = "http://localhost:7004/api/v1/customers";
    private static final String CUSTOMERS_BASE = "http://localhost:7001/api/v1/customers";
    private static final String WAREHOUSES_BASE = "http://localhost:7002/api/v1/warehouses";
    private final String CUST_ID = "c1111111-1111-1111-1111-111111111111";
    private final String VALID_ORDER_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private final String NOTFOUND_ORDER_ID = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private final String CREATED_ORDER_ID = "cccccccc-cccc-cccc-cccc-cccccccccccc";
    private final String UPDATED_ORDER_ID = "dddddddd-dddd-dddd-dddd-dddddddddddd";
    @Autowired
    private WebTestClient webClient;
    @Autowired
    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void getAllOrders_whenUpstreamReturnsList_thenGatewayReturns200AndBody() throws Exception {
        var upstreamJson = """
                [
                  {
                    "orderId":"%1$s",
                    "customerId":"%2$s",
                    "orderDate":"2025-05-01",
                    "warehouseId":"wh1",
                    "totalAmount":100.0,
                    "currency":"USD"
                  }
                ]
                """.formatted(VALID_ORDER_ID, CUST_ID);

        mockServer.expect(once(),
                        requestTo(new URI(ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(upstreamJson, MediaType.APPLICATION_JSON));

        mockServer.expect(once(),
                        requestTo(new URI(CUSTOMERS_BASE + "/" + CUST_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                            {
                              "customerId":"%s",
                              "firstName":"John",
                              "lastName":"Doe",
                              "email":"john.doe@example.com",
                              "phone":"1234567890"
                            }
                        """.formatted(CUST_ID), MediaType.APPLICATION_JSON));

        mockServer.expect(once(),
                        requestTo(new URI(WAREHOUSES_BASE + "/wh1")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                            {
                              "warehouseId":"wh1",
                              "locationName":"Main Warehouse",
                              "address":"123 Warehouse Rd",
                              "capacity":500
                            }
                        """, MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OrderResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(1, list.size());
                    var o = list.get(0);
                    assertAll("order",
                            () -> assertEquals(VALID_ORDER_ID, o.getOrderId()),
                            () -> assertEquals(CUST_ID, o.getCustomerId()),
                            () -> assertEquals(LocalDate.of(2025, 5, 1), o.getOrderDate()),
                            () -> assertEquals("USD", o.getCurrency()),
                            () -> assertEquals("John", o.getFirstName()),
                            () -> assertEquals("Main Warehouse", o.getLocationName())
                    );
                    assertTrue(o.getLinks().hasLink("self"));
                    assertTrue(o.getLinks().hasLink("all-orders"));
                });

        mockServer.verify();
    }

    @Test
    void getAllOrders_whenUpstream404_thenGatewayReturns404() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found upstream\"}"));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void getOrderById_whenValid_thenReturns200AndOrder() throws Exception {
        var upstreamJson = """
                {
                  "orderId":"%1$s",
                  "customerId":"%2$s",
                  "orderDate":"2025-05-01",
                  "warehouseId":"wh1",
                  "totalAmount":100.0,
                  "currency":"USD"
                }
                """.formatted(VALID_ORDER_ID, CUST_ID);

        mockServer.expect(once(),
                        requestTo(new URI(
                                ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + VALID_ORDER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(upstreamJson, MediaType.APPLICATION_JSON));

        mockServer.expect(once(),
                        requestTo(new URI(CUSTOMERS_BASE + "/" + CUST_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                            {
                              "customerId":"%s",
                              "firstName":"Alice",
                              "lastName":"Wonder",
                              "email":"alice@example.com",
                              "phone":"5556667777"
                            }
                        """.formatted(CUST_ID), MediaType.APPLICATION_JSON));

        mockServer.expect(once(),
                        requestTo(new URI(WAREHOUSES_BASE + "/wh1")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                            {
                              "warehouseId":"wh1",
                              "locationName":"East Storage",
                              "address":"789 East Rd",
                              "capacity":200
                            }
                        """, MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + VALID_ORDER_ID)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.orderId").isEqualTo(VALID_ORDER_ID)
                .jsonPath("$.customerId").isEqualTo(CUST_ID)
                .jsonPath("$._links.self.href").isNotEmpty()
                .jsonPath("$._links.all-orders.href").isNotEmpty();

        mockServer.verify();
    }

    @Test
    void getOrderById_whenInvalidUuid_thenReturns422() {
        webClient.get()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH + "/BAD-ID-LEN")
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void getOrderById_whenUpstream404_thenReturns404() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(
                                ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + NOTFOUND_ORDER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found upstream\"}"));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + NOTFOUND_ORDER_ID)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void createOrder_whenValid_thenReturns201AndAllFields() throws Exception {
        var req = OrderRequestModel.builder()
                .warehouseId("wh1")
                .items(List.of(
                        OrderItemRequestModel.builder()
                                .apparelId("aaa11111-bbbb-cccc-dddd-eeeeeeeeeeee")
                                .quantity(2)
                                .unitPrice(new BigDecimal("59.99"))
                                .discount(new BigDecimal("0"))
                                .currency("USD")
                                .build()
                ))
                .build();

        var upstreamResp = """
                {
                  "orderId":"%1$s",
                  "customerId":"%2$s",
                  "orderDate":"2025-05-02",
                  "warehouseId":"wh1",
                  "totalAmount":119.98,
                  "currency":"USD",
                  "orderStatus":"CREATED",
                  "paymentStatus":"PENDING"
                }
                """.formatted(CREATED_ORDER_ID, CUST_ID);

        mockServer.expect(once(),
                        requestTo(new URI(ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(upstreamResp));

        mockServer.expect(once(),
                        requestTo(new URI(CUSTOMERS_BASE + "/" + CUST_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                            {
                              "customerId":"%s",
                              "firstName":"Bob",
                              "lastName":"Builder",
                              "email":"bob@example.com",
                              "phone":"1010101010"
                            }
                        """.formatted(CUST_ID), MediaType.APPLICATION_JSON));

        mockServer.expect(once(),
                        requestTo(new URI(WAREHOUSES_BASE + "/wh1")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                            {
                              "warehouseId":"wh1",
                              "locationName":"Warehouse X",
                              "address":"42 Build Ave",
                              "capacity":300
                            }
                        """, MediaType.APPLICATION_JSON));

        webClient.post()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OrderResponseModel.class)
                .value(o -> {
                    assertAll("created",
                            () -> assertEquals(CREATED_ORDER_ID, o.getOrderId()),
                            () -> assertEquals(CUST_ID, o.getCustomerId()),
                            () -> assertEquals("USD", o.getCurrency()),
                            () -> assertEquals(OrderStatus.CREATED, o.getOrderStatus()),
                            () -> assertEquals(PaymentStatus.PENDING, o.getPaymentStatus())
                    );
                });

        mockServer.verify();
    }

    @Test
    void createOrder_whenUpstream422_thenReturns422() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"invalid input upstream\"}"));

        var req = OrderRequestModel.builder()
                .warehouseId("wh1")
                .items(Collections.emptyList())
                .build();

        webClient.post()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);

        mockServer.verify();
    }

    @Test
    void updateOrder_whenValid_thenReturns200AndUpdated() throws Exception {
        var req = OrderRequestModel.builder()
                .warehouseId("wh1")
                .items(Collections.emptyList())
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(
                                ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + UPDATED_ORDER_ID)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withNoContent());

        var upstreamJson = """
                {
                  "orderId":"%1$s",
                  "customerId":"%2$s",
                  "orderDate":"2025-05-03",
                  "warehouseId":"wh1",
                  "totalAmount":0.0,
                  "currency":"USD",
                  "orderStatus":"COMPLETED",
                  "paymentStatus":"CAPTURED"
                }
                """.formatted(UPDATED_ORDER_ID, CUST_ID);

        mockServer.expect(once(),
                        requestTo(new URI(
                                ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + UPDATED_ORDER_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(upstreamJson, MediaType.APPLICATION_JSON));

        mockServer.expect(once(),
                        requestTo(new URI(CUSTOMERS_BASE + "/" + CUST_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                            {
                              "customerId":"%s",
                              "firstName":"UpdFirst",
                              "lastName":"UpdLast",
                              "email":"upd@example.com",
                              "phone":"2020202020"
                            }
                        """.formatted(CUST_ID), MediaType.APPLICATION_JSON));

        mockServer.expect(once(),
                        requestTo(new URI(WAREHOUSES_BASE + "/wh1")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                            {
                              "warehouseId":"wh1",
                              "locationName":"Updated Warehouse",
                              "address":"100 New St",
                              "capacity":400
                            }
                        """, MediaType.APPLICATION_JSON));

        webClient.put()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + UPDATED_ORDER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OrderResponseModel.class)
                .value(o -> {
                    assertAll("updated",
                            () -> assertEquals(UPDATED_ORDER_ID, o.getOrderId()),
                            () -> assertEquals(OrderStatus.COMPLETED, o.getOrderStatus()),
                            () -> assertEquals(PaymentStatus.CAPTURED, o.getPaymentStatus())
                    );
                });

        mockServer.verify();
    }

    @Test
    void updateOrder_whenUpstream404_thenReturns404() throws Exception {
        var req = OrderRequestModel.builder()
                .warehouseId("wh1")
                .items(Collections.emptyList())
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(
                                ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + NOTFOUND_ORDER_ID)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found upstream\"}"));

        webClient.put()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + NOTFOUND_ORDER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void deleteOrder_whenValid_thenReturns204() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(
                                ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + VALID_ORDER_ID)))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        webClient.delete()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + VALID_ORDER_ID)
                .exchange()
                .expectStatus().isNoContent();

        mockServer.verify();
    }

    @Test
    void deleteOrder_whenUpstream404_thenReturns404() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(
                                ORDERS_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + NOTFOUND_ORDER_ID)))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found upstream\"}"));

        webClient.delete()
                .uri(GATEWAY_BASE + "/" + CUST_ID + ORDERS_PATH + "/" + NOTFOUND_ORDER_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }
}
