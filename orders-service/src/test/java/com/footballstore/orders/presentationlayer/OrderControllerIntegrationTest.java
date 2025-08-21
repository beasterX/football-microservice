    package com.footballstore.orders.presentationlayer;
    
    import static org.junit.jupiter.api.Assertions.*;
    import static org.springframework.test.web.client.ExpectedCount.once;
    import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
    import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
    import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
    
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.databind.SerializationFeature;
    import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
    import com.footballstore.orders.dataaccesslayer.Order;
    import com.footballstore.orders.dataaccesslayer.OrderRepository;
    import com.footballstore.orders.dataaccesslayer.OrderStatus;
    import com.footballstore.orders.dataaccesslayer.PaymentStatus;
    import com.footballstore.orders.domainclientlayer.apparels.ApparelModel;
    import com.footballstore.orders.domainclientlayer.apparels.ApparelType;
    import com.footballstore.orders.domainclientlayer.apparels.ApparelsServiceClient;
    import com.footballstore.orders.domainclientlayer.apparels.SizeOption;
    import com.footballstore.orders.domainclientlayer.customers.Address;
    import com.footballstore.orders.domainclientlayer.customers.ContactMethod;
    import com.footballstore.orders.domainclientlayer.customers.CustomerModel;
    import com.footballstore.orders.domainclientlayer.customers.CustomersServiceClient;
    import com.footballstore.orders.domainclientlayer.warehouses.WarehouseModel;
    import com.footballstore.orders.domainclientlayer.warehouses.WarehousesServiceClient;
    import com.footballstore.orders.presentationlayer.OrderItemRequestModel;
    import com.footballstore.orders.presentationlayer.OrderRequestModel;
    import com.footballstore.orders.presentationlayer.OrderResponseModel;
    import com.footballstore.orders.utils.exceptions.InvalidInputException;
    import com.footballstore.orders.utils.exceptions.NotFoundException;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.test.context.SpringBootTest;
    import org.springframework.data.mongodb.core.MongoTemplate;
    import org.springframework.data.mongodb.core.query.Criteria;
    import org.springframework.data.mongodb.core.query.Query;
    import org.springframework.data.mongodb.core.query.Update;
    import org.springframework.http.HttpMethod;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.MediaType;
    import org.springframework.http.converter.HttpMessageConverter;
    import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
    import org.springframework.test.context.ActiveProfiles;
    import org.springframework.test.context.TestPropertySource;
    import org.springframework.test.web.client.MockRestServiceServer;
    import org.springframework.test.web.reactive.server.WebTestClient;
    import org.springframework.web.client.RestTemplate;
    
    import java.math.BigDecimal;
    import java.net.URI;
    import java.time.LocalDate;
    import java.util.List;
    
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @ActiveProfiles("test")
    @TestPropertySource(properties = {
            "app.customers-service.host=localhost",
            "app.customers-service.port=7001",
            "app.warehouses-service.host=localhost",
            "app.warehouses-service.port=7002",
            "app.apparels-service.host=localhost",
            "app.apparels-service.port=7003"
    })
    class OrderControllerIntegrationTest {
    
        @Autowired private WebTestClient webClient;
        @Autowired private RestTemplate restTemplate;
        @Autowired private OrderRepository orderRepository;
        @Autowired private MongoTemplate mongoTemplate;
        @Autowired private CustomersServiceClient customersClient;
        @Autowired private WarehousesServiceClient warehousesClient;
        @Autowired private ApparelsServiceClient apparelsClient;
    
        private MockRestServiceServer mockServer;
        private ObjectMapper mapper;
    
        private static final String BASE_URI = "/api/v1/customers";
        private static final String CUST_SERVICE_BASE_URI = "http://localhost:7001/api/v1/customers";
        private static final String WH_SERVICE_BASE_URI = "http://localhost:7002/api/v1/warehouses";
        private static final String APP_SERVICE_BASE_URI = "http://localhost:7003/api/v1/apparels";
    
        private String existingCustomerId;
        private String existingOrderId;
        private String existingWarehouseId;
        private String existingApparelId;
    
        private static final String INVALID_UUID = "bad-uuid";
        private static final String NOT_FOUND_UUID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    
        @BeforeEach
        void init() {
            mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            for (HttpMessageConverter<?> conv : restTemplate.getMessageConverters()) {
                if (conv instanceof MappingJackson2HttpMessageConverter mj) {
                    mj.setObjectMapper(mapper);
                }
            }
            mockServer = MockRestServiceServer.createServer(restTemplate);
            assertTrue(orderRepository.count() > 0);
            Order sample = orderRepository.findAll().get(0);
            existingCustomerId  = sample.getCustomerModel().getCustomerId();
            existingOrderId     = sample.getOrderIdentifier().getOrderId();
            existingWarehouseId = sample.getWarehouseModel().getWarehouseId();
            existingApparelId   = sample.getItems().get(0).getApparelModel().getApparelId();
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("orderIdentifier.orderId").is(existingOrderId)),
                    new Update().set("orderStatus",  OrderStatus.CREATED)
                            .set("paymentStatus",PaymentStatus.PENDING),
                    Order.class
            );
        }
    
        @Test
        void whenGetAllOrdersExists_thenReturnListOfOrders() {
            long expected = orderRepository.findAllByCustomerModel_CustomerId(existingCustomerId).size();
            webClient.get().uri(BASE_URI + "/" + existingCustomerId + "/orders")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange().expectStatus().isOk()
                    .expectBodyList(OrderResponseModel.class)
                    .value(list -> {
                        assertNotNull(list);
                        assertEquals(expected, list.size());
                    });
        }
    
        @Test
        void whenGetAllOrdersWithInvalidCustomerId_thenReturnUnprocessableEntity() {
            webClient.get().uri(BASE_URI + "/" + INVALID_UUID + "/orders")
                    .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                    .expectBody().jsonPath("$.message")
                    .isEqualTo("Invalid customerId: " + INVALID_UUID);
        }
    
        @Test
        void whenGetAllOrdersCustomerNotFound_thenReturnEmptyList() {
            webClient.get().uri(BASE_URI + "/" + NOT_FOUND_UUID + "/orders")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange().expectStatus().isOk()
                    .expectBodyList(OrderResponseModel.class)
                    .value(List::isEmpty);
        }
    
        @Test
        void whenGetOrderByIdExists_thenReturnOrder() {
            webClient.get().uri(BASE_URI + "/" + existingCustomerId + "/orders/" + existingOrderId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange().expectStatus().isOk()
                    .expectBody(OrderResponseModel.class)
                    .value(resp -> assertEquals(existingOrderId, resp.getOrderId()));
        }
    
        @Test
        void whenGetOrderByIdWithInvalidCustomerId_thenReturnUnprocessableEntity() {
            webClient.get().uri(BASE_URI + "/" + INVALID_UUID + "/orders/" + existingOrderId)
                    .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                    .expectBody().jsonPath("$.message").isEqualTo("Invalid IDs");
        }
    
        @Test
        void whenGetOrderByIdWithInvalidOrderId_thenReturnUnprocessableEntity() {
            webClient.get().uri(BASE_URI + "/" + existingCustomerId + "/orders/" + INVALID_UUID)
                    .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                    .expectBody().jsonPath("$.message").isEqualTo("Invalid IDs");
        }
    
        @Test
        void whenGetOrderByIdNotFound_thenReturnNotFound() {
            webClient.get().uri(BASE_URI + "/" + existingCustomerId + "/orders/" + NOT_FOUND_UUID)
                    .exchange().expectStatus().isNotFound();
        }
    
        @Test
        void whenCreateOrderWithValidData_thenReturnCreatedOrder() throws Exception {
            CustomerModel cm = CustomerModel.builder()
                    .customerId(existingCustomerId)
                    .firstName("X").lastName("Y")
                    .email("x@y.com").phone("000")
                    .registrationDate(LocalDate.now())
                    .preferredContact(ContactMethod.EMAIL)
                    .address(new Address("st","ct","st","co","pc"))
                    .build();
            mockServer.expect(once(),
                            requestTo(new URI(CUST_SERVICE_BASE_URI + "/" + existingCustomerId)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(mapper.writeValueAsString(cm),MediaType.APPLICATION_JSON));
            WarehouseModel wm = WarehouseModel.builder()
                    .warehouseId(existingWarehouseId)
                    .locationName("L")
                    .address("A")
                    .capacity(10)
                    .build();
            mockServer.expect(once(),
                            requestTo(new URI(WH_SERVICE_BASE_URI + "/" + existingWarehouseId)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(mapper.writeValueAsString(wm),MediaType.APPLICATION_JSON));
            ApparelModel am = ApparelModel.builder()
                    .apparelId(existingApparelId)
                    .itemName("N").description("D").brand("B")
                    .price(new BigDecimal("9.99")).cost(new BigDecimal("4.44"))
                    .stock(5).apparelType(ApparelType.JERSEY)
                    .sizeOption(SizeOption.M)
                    .build();
            mockServer.expect(once(),
                            requestTo(new URI(APP_SERVICE_BASE_URI + "/" + existingApparelId)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(mapper.writeValueAsString(am),MediaType.APPLICATION_JSON));
            mockServer.expect(once(),
                            requestTo(new URI(APP_SERVICE_BASE_URI + "/" + existingApparelId + "/stock")))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("5",MediaType.APPLICATION_JSON));
            mockServer.expect(once(),
                            requestTo(APP_SERVICE_BASE_URI + "/" + existingApparelId + "/stock/decrease?quantity=1"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andRespond(withSuccess());
            OrderRequestModel req = OrderRequestModel.builder()
                    .warehouseId(existingWarehouseId)
                    .items(List.of(
                            OrderItemRequestModel.builder()
                                    .apparelId(existingApparelId)
                                    .quantity(1)
                                    .unitPrice(new BigDecimal("9.99"))
                                    .discount(BigDecimal.ZERO)
                                    .currency("USD")
                                    .build()
                    ))
                    .build();
            webClient.post().uri(BASE_URI + "/" + existingCustomerId + "/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .exchange().expectStatus().isCreated()
                    .expectBody(OrderResponseModel.class)
                    .value(resp -> {
                        assertNotNull(resp.getOrderId());
                        assertEquals("USD", resp.getCurrency());
                        assertEquals("9.99", resp.getTotalAmount().toString());
                    });
            mockServer.verify();
        }
    
        @Test
        void whenCreateOrderCustomerNotFound_thenReturnNotFound() throws Exception {
            mockServer.expect(once(),
                            requestTo(new URI(CUST_SERVICE_BASE_URI + "/" + existingCustomerId)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND));
            OrderRequestModel req = OrderRequestModel.builder()
                    .warehouseId(existingWarehouseId)
                    .items(List.of())
                    .build();
            webClient.post().uri(BASE_URI + "/" + existingCustomerId + "/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .exchange().expectStatus().isNotFound();
            mockServer.verify();
        }
    
        @Test
        void whenCreateOrderWarehouseNotFound_thenReturnNotFound() throws Exception {
            CustomerModel cm = CustomerModel.builder().customerId(existingCustomerId).build();
            mockServer.expect(once(),
                            requestTo(new URI(CUST_SERVICE_BASE_URI + "/" + existingCustomerId)))
                    .andRespond(withSuccess(mapper.writeValueAsString(cm),MediaType.APPLICATION_JSON));
            mockServer.expect(once(),
                            requestTo(new URI(WH_SERVICE_BASE_URI + "/" + existingWarehouseId)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND));
            OrderRequestModel req = OrderRequestModel.builder()
                    .warehouseId(existingWarehouseId)
                    .items(List.of())
                    .build();
            webClient.post().uri(BASE_URI + "/" + existingCustomerId + "/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .exchange().expectStatus().isNotFound();
            mockServer.verify();
        }
    
        @Test
        void whenCreateOrderApparelNotFound_thenReturnNotFound() throws Exception {
            CustomerModel cm = CustomerModel.builder().customerId(existingCustomerId).build();
            WarehouseModel wm = WarehouseModel.builder().warehouseId(existingWarehouseId).build();
            mockServer.expect(once(),
                            requestTo(new URI(CUST_SERVICE_BASE_URI + "/" + existingCustomerId)))
                    .andRespond(withSuccess(mapper.writeValueAsString(cm),MediaType.APPLICATION_JSON));
            mockServer.expect(once(),
                            requestTo(new URI(WH_SERVICE_BASE_URI + "/" + existingWarehouseId)))
                    .andRespond(withSuccess(mapper.writeValueAsString(wm),MediaType.APPLICATION_JSON));
            mockServer.expect(once(),
                            requestTo(new URI(APP_SERVICE_BASE_URI + "/" + existingApparelId)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND));
            OrderRequestModel req = OrderRequestModel.builder()
                    .warehouseId(existingWarehouseId)
                    .items(List.of(
                            OrderItemRequestModel.builder()
                                    .apparelId(existingApparelId)
                                    .quantity(1)
                                    .unitPrice(BigDecimal.ONE)
                                    .discount(BigDecimal.ZERO)
                                    .currency("USD")
                                    .build()
                    ))
                    .build();
            webClient.post().uri(BASE_URI + "/" + existingCustomerId + "/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .exchange().expectStatus().isNotFound();
            mockServer.verify();
        }
    
        @Test
        void whenCreateOrderInsufficientStock_thenReturnUnprocessableEntity() throws Exception {
            CustomerModel cm = CustomerModel.builder().customerId(existingCustomerId).build();
            WarehouseModel wm = WarehouseModel.builder().warehouseId(existingWarehouseId).build();
            ApparelModel am = ApparelModel.builder().apparelId(existingApparelId).build();
            mockServer.expect(once(),
                            requestTo(new URI(CUST_SERVICE_BASE_URI + "/" + existingCustomerId)))
                    .andRespond(withSuccess(mapper.writeValueAsString(cm),MediaType.APPLICATION_JSON));
            mockServer.expect(once(),
                            requestTo(new URI(WH_SERVICE_BASE_URI + "/" + existingWarehouseId)))
                    .andRespond(withSuccess(mapper.writeValueAsString(wm),MediaType.APPLICATION_JSON));
            mockServer.expect(once(),
                            requestTo(new URI(APP_SERVICE_BASE_URI + "/" + existingApparelId)))
                    .andRespond(withSuccess(mapper.writeValueAsString(am),MediaType.APPLICATION_JSON));
            mockServer.expect(once(),
                            requestTo(new URI(APP_SERVICE_BASE_URI + "/" + existingApparelId + "/stock")))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("0",MediaType.APPLICATION_JSON));
            OrderRequestModel req = OrderRequestModel.builder()
                    .warehouseId(existingWarehouseId)
                    .items(List.of(
                            OrderItemRequestModel.builder()
                                    .apparelId(existingApparelId)
                                    .quantity(1)
                                    .unitPrice(BigDecimal.ONE)
                                    .discount(BigDecimal.ZERO)
                                    .currency("USD")
                                    .build()
                    ))
                    .build();
            webClient.post().uri(BASE_URI + "/" + existingCustomerId + "/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                    .expectBody().jsonPath("$.message")
                    .isEqualTo("Not enough stock for " + existingApparelId);
            mockServer.verify();
        }
    
        @Test
        void whenUpdateOrderWithValidData_thenReturnOk() throws Exception {
            ApparelModel am = ApparelModel.builder()
                    .apparelId(existingApparelId)
                    .itemName("Dummy")
                    .description("Desc")
                    .brand("Brand")
                    .price(new BigDecimal("9.99"))
                    .cost(new BigDecimal("4.44"))
                    .stock(5)
                    .apparelType(ApparelType.JERSEY)
                    .sizeOption(SizeOption.M)
                    .build();
            mockServer.expect(once(),
                            requestTo(new URI(APP_SERVICE_BASE_URI + "/" + existingApparelId)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(mapper.writeValueAsString(am),MediaType.APPLICATION_JSON));
            OrderRequestModel req = OrderRequestModel.builder()
                    .warehouseId(existingWarehouseId)
                    .items(List.of(
                            OrderItemRequestModel.builder()
                                    .apparelId(existingApparelId)
                                    .quantity(1)
                                    .unitPrice(BigDecimal.ZERO)
                                    .discount(BigDecimal.ZERO)
                                    .currency("USD")
                                    .build()
                    ))
                    .build();
            webClient.put().uri(BASE_URI + "/" + existingCustomerId + "/orders/" + existingOrderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .exchange().expectStatus().isOk()
                    .expectBody(OrderResponseModel.class)
                    .value(resp -> assertEquals(existingOrderId, resp.getOrderId()));
            mockServer.verify();
        }
    
        @Test
        void whenUpdateOrderWithInvalidIds_thenReturnNotFound() {
            OrderRequestModel req = OrderRequestModel.builder()
                    .warehouseId(existingWarehouseId)
                    .items(List.of())
                    .build();
            webClient.put().uri(BASE_URI + "/bad-cust/orders/bad-order")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .exchange().expectStatus().isNotFound();
        }
    
        @Test
        void whenUpdateOrderNotFound_thenReturnNotFound() {
            OrderRequestModel req = OrderRequestModel.builder()
                    .warehouseId(existingWarehouseId)
                    .items(List.of())
                    .build();
            webClient.put().uri(BASE_URI + "/" + existingCustomerId + "/orders/" + NOT_FOUND_UUID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .exchange().expectStatus().isNotFound();
        }
    
        @Test
        void whenUpdateOrderInsufficientStock_thenReturnUnprocessableEntity() throws Exception {
            mockServer.expect(once(),
                            requestTo(new URI(APP_SERVICE_BASE_URI + "/" + existingApparelId + "/stock")))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("0",MediaType.APPLICATION_JSON));
            OrderRequestModel req = OrderRequestModel.builder()
                    .warehouseId(existingWarehouseId)
                    .items(List.of(
                            OrderItemRequestModel.builder()
                                    .apparelId(existingApparelId)
                                    .quantity(100)
                                    .unitPrice(BigDecimal.ZERO)
                                    .discount(BigDecimal.ZERO)
                                    .currency("USD")
                                    .build()
                    ))
                    .build();
            webClient.put().uri(BASE_URI + "/" + existingCustomerId + "/orders/" + existingOrderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req)
                    .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                    .expectBody().jsonPath("$.message")
                    .value(msg -> assertTrue(((String) msg).contains("Not enough stock")));
            mockServer.verify();
        }
    
        @Test
        void whenDeleteOrderWithValidData_thenReturnNoContent() throws Exception {
            mockServer.expect(once(),
                            requestTo(new URI(APP_SERVICE_BASE_URI + "/" + existingApparelId + "/stock/increase?quantity=1")))
                    .andExpect(method(HttpMethod.PATCH))
                    .andRespond(withSuccess());
            webClient.delete().uri(BASE_URI + "/" + existingCustomerId + "/orders/" + existingOrderId)
                    .exchange().expectStatus().isNoContent();
            mockServer.verify();
        }
    
        @Test
        void whenDeleteOrderWithInvalidCustomerId_thenReturnUnprocessableEntity() {
            webClient.delete().uri(BASE_URI + "/bad-uuid/orders/" + existingOrderId)
                    .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                    .expectBody().jsonPath("$.message")
                    .isEqualTo("Invalid customerId: bad-uuid");
        }
    
        @Test
        void whenDeleteOrderWithInvalidOrderId_thenReturnUnprocessableEntity() {
            webClient.delete().uri(BASE_URI + "/" + existingCustomerId + "/orders/" + INVALID_UUID)
                    .exchange().expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                    .expectBody().jsonPath("$.message")
                    .isEqualTo("Invalid orderId: " + INVALID_UUID);
        }
    
        @Test
        void whenDeleteOrderNotFound_thenReturnNotFound() {
            webClient.delete().uri(BASE_URI + "/" + existingCustomerId + "/orders/" + NOT_FOUND_UUID)
                    .exchange().expectStatus().isNotFound();
        }
    
        @Test
        void apparelClient_getAllApparels_returnsList() throws Exception {
            ApparelModel a1 = ApparelModel.builder()
                    .apparelId("A").itemName("X").description("D").brand("B")
                    .price(new BigDecimal("10.00")).cost(new BigDecimal("5.00"))
                    .stock(2).apparelType(ApparelType.JERSEY).sizeOption(SizeOption.M)
                    .build();
            ApparelModel a2 = ApparelModel.builder()
                    .apparelId("B").itemName("Y").description("D2").brand("C")
                    .price(new BigDecimal("20.00")).cost(new BigDecimal("8.00"))
                    .stock(3).apparelType(ApparelType.SHORTS).sizeOption(SizeOption.L)
                    .build();
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                            mapper.writeValueAsString(new ApparelModel[]{a1,a2}),
                            MediaType.APPLICATION_JSON));
            List<ApparelModel> list = apparelsClient.getAllApparels();
            assertEquals(2, list.size());
            assertEquals("A", list.get(0).getApparelId());
            mockServer.verify();
        }
    
        @Test
        void apparelClient_getAllApparels_errorBranches() {
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"no apparels\"}"));
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"bad input\"}"));
            assertThrows(NotFoundException.class,     () -> apparelsClient.getAllApparels());
            assertThrows(InvalidInputException.class, () -> apparelsClient.getAllApparels());
            mockServer.verify();
        }
    
        @Test
        void apparelClient_getApparelById_404_throwsNotFound() {
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/" + existingApparelId))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"no apparel\"}"));
            assertThrows(NotFoundException.class,
                    () -> apparelsClient.getApparelByApparelId(existingApparelId));
            mockServer.verify();
        }
    
        @Test
        void apparelClient_getApparelById_422_throwsInvalidInput() {
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/" + existingApparelId))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"bad id\"}"));
            assertThrows(InvalidInputException.class,
                    () -> apparelsClient.getApparelByApparelId(existingApparelId));
            mockServer.verify();
        }
    
        @Test
        void apparelClient_createUpdateDelete_andStockOperations() throws Exception {
            ApparelModel newApp = ApparelModel.builder()
                    .apparelId("NEW").itemName("N").description("d")
                    .brand("X").price(new BigDecimal("5.00"))
                    .cost(new BigDecimal("2.00"))
                    .stock(10).apparelType(ApparelType.SHOES).sizeOption(SizeOption.XS)
                    .build();
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(
                            mapper.writeValueAsString(newApp),
                            MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/NEW"))
                    .andExpect(method(HttpMethod.PUT))
                    .andRespond(withSuccess());
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/NEW"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                            mapper.writeValueAsString(newApp),
                            MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/NEW"))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withSuccess());
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/NEW/stock"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("8",MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/NEW/stock/decrease?quantity=2"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andRespond(withSuccess());
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/NEW/stock/increase?quantity=5"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andRespond(withSuccess());
            var created = apparelsClient.createApparel(newApp);
            assertEquals("NEW", created.getApparelId());
            var updated = apparelsClient.updateApparel("NEW", newApp);
            assertEquals("NEW", updated.getApparelId());
            assertDoesNotThrow(() -> apparelsClient.deleteApparel("NEW"));
            int stock = apparelsClient.getStock("NEW");
            assertEquals(8, stock);
            apparelsClient.decreaseStock("NEW", 2);
            apparelsClient.increaseStock("NEW", 5);
            mockServer.verify();
        }
    
        @Test
        void apparelClient_createUpdateDelete_errorBranches() {
            ApparelModel newApp = ApparelModel.builder()
                    .apparelId("A42").itemName("X").description("D").brand("B")
                    .price(new BigDecimal("1.00")).cost(new BigDecimal("0.50"))
                    .stock(10).apparelType(ApparelType.JERSEY).sizeOption(SizeOption.M)
                    .build();
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"no app\"}"));
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"bad app\"}"));
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/A42"))
                    .andExpect(method(HttpMethod.PUT))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"gone\"}"));
            mockServer.expect(once(), requestTo(APP_SERVICE_BASE_URI + "/A42"))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"cannot delete\"}"));
            assertThrows(NotFoundException.class,     () -> apparelsClient.createApparel(newApp));
            assertThrows(InvalidInputException.class, () -> apparelsClient.createApparel(newApp));
            assertThrows(NotFoundException.class,     () -> apparelsClient.updateApparel("A42", newApp));
            assertThrows(InvalidInputException.class, () -> apparelsClient.deleteApparel("A42"));
            mockServer.verify();
        }
    
        @Test
        void customerClient_getAllAndById_andErrorBranches() throws Exception {
            CustomerModel c1 = CustomerModel.builder()
                    .customerId("C1").firstName("A").lastName("B")
                    .email("a@b.com").phone("123")
                    .registrationDate(LocalDate.now())
                    .preferredContact(ContactMethod.EMAIL)
                    .address(new Address("s","c","st","co","pc"))
                    .build();
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                            mapper.writeValueAsString(new CustomerModel[]{c1}),
                            MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI + "/C1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                            mapper.writeValueAsString(c1),
                            MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI + "/C1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"no cust\"}"));
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI + "/C1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"bad cust\"}"));
            var all = customersClient.getAllCustomers();
            assertEquals(1, all.size());
            var byId = customersClient.getCustomerByCustomerId("C1");
            assertEquals("C1", byId.getCustomerId());
            assertThrows(NotFoundException.class,
                    () -> customersClient.getCustomerByCustomerId("C1"));
            assertThrows(InvalidInputException.class,
                    () -> customersClient.getCustomerByCustomerId("C1"));
            mockServer.verify();
        }
    
        @Test
        void customerClient_positivePathCRUD() throws Exception {
            CustomerModel newCust = CustomerModel.builder()
                    .customerId("C42").firstName("Z").lastName("Y")
                    .email("z@y.com").phone("999")
                    .registrationDate(LocalDate.now())
                    .preferredContact(ContactMethod.EMAIL)
                    .address(new Address("sst","cty","stt","con","pcd"))
                    .build();
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(mapper.writeValueAsString(newCust),MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI + "/C42"))
                    .andExpect(method(HttpMethod.PUT))
                    .andRespond(withSuccess());
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI + "/C42"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(mapper.writeValueAsString(newCust),MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI + "/C42"))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withSuccess());
            var created = customersClient.createCustomer(newCust);
            assertEquals("C42", created.getCustomerId());
            var updated = customersClient.updateCustomer("C42", newCust);
            assertEquals("C42", updated.getCustomerId());
            assertDoesNotThrow(() -> customersClient.deleteCustomer("C42"));
            mockServer.verify();
        }
    
        @Test
        void customerClient_errorBranches() {
            CustomerModel newCust = CustomerModel.builder()
                    .customerId("C42").build();
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"no cust\"}"));
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"bad cust\"}"));
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI + "/C42"))
                    .andExpect(method(HttpMethod.PUT))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"not found\"}"));
            mockServer.expect(once(), requestTo(CUST_SERVICE_BASE_URI + "/C42"))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"invalid delete\"}"));
            assertThrows(NotFoundException.class,     () -> customersClient.createCustomer(newCust));
            assertThrows(InvalidInputException.class, () -> customersClient.createCustomer(newCust));
            assertThrows(NotFoundException.class,     () -> customersClient.updateCustomer("C42", newCust));
            assertThrows(InvalidInputException.class, () -> customersClient.deleteCustomer("C42"));
            mockServer.verify();
        }
    
        @Test
        void warehouseClient_getAllAndById_andErrorBranches_andStockOps() throws Exception {
            WarehouseModel w1 = WarehouseModel.builder()
                    .warehouseId("W1").locationName("L")
                    .address("A").capacity(200)
                    .build();
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                            mapper.writeValueAsString(new WarehouseModel[]{w1}),
                            MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(
                            mapper.writeValueAsString(w1),
                            MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W1"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"no wh\"}"));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W1/apparels/" + existingApparelId + "/stock"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("7",MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W1/apparels/" + existingApparelId + "/stock"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("7",MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W1/apparels/" + existingApparelId + "/stock/decrease?quantity=2"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andRespond(withSuccess());
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W1/apparels/" + existingApparelId + "/stock/increase?quantity=3"))
                    .andExpect(method(HttpMethod.PATCH))
                    .andRespond(withSuccess());
            var all = warehousesClient.getAllWarehouses();
            assertEquals(1, all.size());
            var byId = warehousesClient.getWarehouseByWarehouseId("W1");
            assertEquals("W1", byId.getWarehouseId());
            assertThrows(NotFoundException.class,
                    () -> warehousesClient.getWarehouseByWarehouseId("W1"));
            int stock = warehousesClient.getStock("W1", existingApparelId);
            assertEquals(7, stock);
            assertTrue(warehousesClient.isInStock("W1", existingApparelId, 5));
            warehousesClient.decreaseStock("W1", existingApparelId, 2);
            warehousesClient.increaseStock("W1", existingApparelId, 3);
            mockServer.verify();
        }
    
        @Test
        void warehouseClient_positivePathCRUD() throws Exception {
            WarehouseModel newWh = WarehouseModel.builder()
                    .warehouseId("W42")
                    .locationName("L")
                    .address("A")
                    .capacity(123)
                    .build();
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(mapper.writeValueAsString(newWh),MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W42"))
                    .andExpect(method(HttpMethod.PUT))
                    .andRespond(withSuccess());
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W42"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(mapper.writeValueAsString(newWh),MediaType.APPLICATION_JSON));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W42"))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withSuccess());
            var created = warehousesClient.createWarehouse(newWh);
            assertEquals("W42", created.getWarehouseId());
            var updated = warehousesClient.updateWarehouse("W42", newWh);
            assertEquals("W42", updated.getWarehouseId());
            assertDoesNotThrow(() -> warehousesClient.deleteWarehouse("W42"));
            mockServer.verify();
        }
    
        @Test
        void warehouseClient_errorBranches() {
            WarehouseModel newWh = WarehouseModel.builder()
                    .warehouseId("W42").build();
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"no wh\"}"));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"invalid wh\"}"));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W42"))
                    .andExpect(method(HttpMethod.PUT))
                    .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"bad update\"}"));
            mockServer.expect(once(), requestTo(WH_SERVICE_BASE_URI + "/W42"))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body("{\"message\":\"gone\"}"));
            assertThrows(NotFoundException.class,     () -> warehousesClient.createWarehouse(newWh));
            assertThrows(InvalidInputException.class, () -> warehousesClient.createWarehouse(newWh));
            assertThrows(InvalidInputException.class, () -> warehousesClient.updateWarehouse("W42", newWh));
            assertThrows(NotFoundException.class,     () -> warehousesClient.deleteWarehouse("W42"));
            mockServer.verify();
        }
    }
