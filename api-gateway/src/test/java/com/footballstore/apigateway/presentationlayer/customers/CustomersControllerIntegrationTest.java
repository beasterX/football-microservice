package com.footballstore.apigateway.presentationlayer.customers;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.customers-service.host=localhost",
        "app.customers-service.port=7001"
})
class  CustomersControllerIntegrationTest {

    private static final String GATEWAY_BASE = "/api/v1/customers";
    private static final String UPSTREAM_BASE = "http://localhost:7001/api/v1/customers";

    @Autowired
    private WebTestClient webTestClient;

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
    void getAllCustomers_whenUpstreamReturnsList_thenGatewayReturns200AndBody() throws Exception {
        String upstreamJson = """
        [
          {
            "customerId":"11111111-1111-1111-1111-111111111111",
            "firstName":"John",
            "lastName":"Doe",
            "email":"john.doe@example.com",
            "phone":"1234567890"
          },
          {
            "customerId":"22222222-2222-2222-2222-222222222222",
            "firstName":"Jane",
            "lastName":"Smith",
            "email":"jane.smith@example.com",
            "phone":"0987654321"
          }
        ]
        """;

        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(upstreamJson, MediaType.APPLICATION_JSON));

        webTestClient.get()
                .uri(GATEWAY_BASE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                    var c1 = list.get(0);
                    assertAll("first customer",
                            () -> assertEquals("11111111-1111-1111-1111-111111111111", c1.getCustomerId()),
                            () -> assertEquals("John", c1.getFirstName()),
                            () -> assertEquals("Doe", c1.getLastName()),
                            () -> assertEquals("john.doe@example.com", c1.getEmail()),
                            () -> assertEquals("1234567890", c1.getPhone())
                    );
                });

        mockServer.verify();
    }

    @Test
    void getAllCustomers_whenUpstreamNotFound_thenGatewayReturns404() {
        mockServer.expect(once(), requestTo(UPSTREAM_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found upstream\"}"));

        webTestClient.get()
                .uri(GATEWAY_BASE)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void getCustomerById_whenValid_thenReturns200AndCustomer() throws Exception {
        String id = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        String upstreamJson = """
        {
          "customerId":"%s",
          "firstName":"Alice",
          "lastName":"Wonder",
          "email":"alice.wonder@example.com",
          "phone":"5556667777"
        }
        """.formatted(id);

        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(upstreamJson, MediaType.APPLICATION_JSON));

        webTestClient.get()
                .uri(GATEWAY_BASE + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(c -> {
                    assertAll("customer",
                            () -> assertEquals(id, c.getCustomerId()),
                            () -> assertEquals("Alice", c.getFirstName()),
                            () -> assertEquals("Wonder", c.getLastName()),
                            () -> assertEquals("alice.wonder@example.com", c.getEmail()),
                            () -> assertEquals("5556667777", c.getPhone())
                    );
                });

        mockServer.verify();
    }

    @Test
    void getCustomerById_whenInvalidUuid_thenReturns422() {
        webTestClient.get()
                .uri(GATEWAY_BASE + "/BAD-UUID")
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void getCustomerById_whenUpstream404_thenReturns404() throws Exception {
        String id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found upstream\"}"));

        webTestClient.get()
                .uri(GATEWAY_BASE + "/" + id)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void createCustomer_whenValid_thenReturns201AndAllFields() throws Exception {
        CustomerRequestModel req = CustomerRequestModel.builder()
                .firstName("Bob")
                .lastName("Builder")
                .email("bob.builder@example.com")
                .phone("1010101010")
                .street("42 Build Ave")
                .city("Construct City")
                .state("UT")
                .postalCode("84000")
                .country("USA")
                .build();

        String upstreamResp = """
        {
          "customerId":"cccccccc-cccc-cccc-cccc-cccccccccccc",
          "firstName":"Bob",
          "lastName":"Builder",
          "email":"bob.builder@example.com",
          "phone":"1010101010"
        }
        """;

        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(upstreamResp));

        webTestClient.post()
                .uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponseModel.class)
                .value(c -> {
                    assertAll("created",
                            () -> assertNotNull(c.getCustomerId()),
                            () -> assertEquals("Bob", c.getFirstName()),
                            () -> assertEquals("Builder", c.getLastName()),
                            () -> assertEquals("bob.builder@example.com", c.getEmail()),
                            () -> assertEquals("1010101010", c.getPhone())
                    );
                });

        mockServer.verify();
    }

    @Test
    void createCustomer_whenUpstream422_thenReturns422() throws Exception {
        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"invalid input upstream\"}"));

        CustomerRequestModel req = CustomerRequestModel.builder()
                .firstName("X")
                .lastName("Y")
                .email("x@y") // invalid
                .phone("0")
                .street("S")
                .city("C")
                .state("ST")
                .postalCode("PC")
                .country("CT")
                .build();

        webTestClient.post()
                .uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);

        mockServer.verify();
    }

    @Test
    void updateCustomer_whenValid_thenReturns200AndUpdated() throws Exception {
        String id = "dddddddd-dddd-dddd-dddd-dddddddddddd";
        CustomerRequestModel req = CustomerRequestModel.builder()
                .firstName("Updated")
                .lastName("User")
                .email("updated.user@example.com")
                .phone("2020202020")
                .street("100 New St")
                .city("New City")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();

        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withNoContent());

        String upstreamJson = """
        {
          "customerId":"%1$s",
          "firstName":"Updated",
          "lastName":"User",
          "email":"updated.user@example.com",
          "phone":"2020202020"
        }
        """.formatted(id);

        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(upstreamJson, MediaType.APPLICATION_JSON));

        webTestClient.put()
                .uri(GATEWAY_BASE + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(c -> {
                    assertAll("updated",
                            () -> assertEquals(id, c.getCustomerId()),
                            () -> assertEquals("Updated", c.getFirstName()),
                            () -> assertEquals("User", c.getLastName()),
                            () -> assertEquals("updated.user@example.com", c.getEmail()),
                            () -> assertEquals("2020202020", c.getPhone())
                    );
                });

        mockServer.verify();
    }

    @Test
    void updateCustomer_whenUpstream404_thenReturns404() throws Exception {
        String id = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee";
        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found upstream\"}"));

        CustomerRequestModel req = CustomerRequestModel.builder()
                .firstName("Nope").lastName("Nope")
                .email("nope@example.com").phone("000")
                .street("S").city("C").state("ST")
                .postalCode("PC").country("CT")
                .build();

        webTestClient.put()
                .uri(GATEWAY_BASE + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void deleteCustomer_whenValid_thenReturns204() throws Exception {
        String id = "ffffffff-ffff-ffff-ffff-ffffffffffff";
        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        webTestClient.delete()
                .uri(GATEWAY_BASE + "/" + id)
                .exchange()
                .expectStatus().isNoContent();

        mockServer.verify();
    }

    @Test
    void deleteCustomer_whenUpstream404_thenReturns404() throws Exception {
        String id = "99999999-9999-9999-9999-999999999999";
        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found upstream\"}"));

        webTestClient.delete()
                .uri(GATEWAY_BASE + "/" + id)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }
}
