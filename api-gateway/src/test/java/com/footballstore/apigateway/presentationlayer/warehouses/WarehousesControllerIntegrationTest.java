package com.footballstore.apigateway.presentationlayer.warehouses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.hateoas.MediaTypes;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.warehouses-service.host=localhost",
        "app.warehouses-service.port=7004"
})
class WarehousesControllerIntegrationTest {

    private static final String GATEWAY_BASE = "/api/v1/warehouses";
    private static final String UPSTREAM_BASE = "http://localhost:7004/api/v1/warehouses";
    private final String VALID_ID = "11111111-2222-3333-4444-555555555555";
    private final String NOTFOUND_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
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
    void getAllWarehouses_whenUpstreamReturnsList_then200AndBody() throws Exception {
        var upstreamJson = """
                [
                  {
                    "warehouseId":"%1$s",
                    "locationName":"LocA",
                    "address":"AddrA",
                    "capacity":500
                  }
                ]
                """.formatted(VALID_ID);

        mockServer.expect(once(),
                        requestTo(new URI(UPSTREAM_BASE)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(upstreamJson, MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_BASE)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WarehouseResponseModel.class)
                .value(list -> {
                    assertEquals(1, list.size());
                    var w = list.get(0);
                    assertEquals(VALID_ID, w.getWarehouseId());
                    assertTrue(w.getLinks().hasLink("self"));
                    assertTrue(w.getLinks().hasLink("allWarehouses"));
                });

        mockServer.verify();
    }

    @Test
    void getAllWarehouses_whenUpstream404_thenReturns404() {
        mockServer.expect(once(),
                        requestTo(UPSTREAM_BASE))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found\"}"));

        webClient.get()
                .uri(GATEWAY_BASE)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void getWarehouseById_whenValid_then200AndBody() throws Exception {
        var upstreamJson = """
                {
                  "warehouseId":"%1$s",
                  "locationName":"LocB",
                  "address":"AddrB",
                  "capacity":750
                }
                """.formatted(VALID_ID);

        mockServer.expect(once(),
                        requestTo(new URI(UPSTREAM_BASE + "/" + VALID_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(upstreamJson, MediaType.APPLICATION_JSON));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + VALID_ID)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.warehouseId").isEqualTo(VALID_ID)
                .jsonPath("$.locationName").isEqualTo("LocB")
                .jsonPath("$._links.self.href").isNotEmpty()
                .jsonPath("$._links.allWarehouses.href").isNotEmpty();

        mockServer.verify();
    }

    @Test
    void getWarehouseById_whenInvalidUuid_thenReturns422() {
        webClient.get()
                .uri(GATEWAY_BASE + "/BAD-ID")
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void getWarehouseById_whenUpstream404_thenReturns404() {
        mockServer.expect(once(),
                        requestTo(UPSTREAM_BASE + "/" + NOTFOUND_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found\"}"));

        webClient.get()
                .uri(GATEWAY_BASE + "/" + NOTFOUND_ID)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void createWarehouse_whenValid_then201AndBody() throws Exception {
        var req = WarehouseRequestModel.builder()
                .locationName("LocC")
                .address("AddrC")
                .capacity(300)
                .build();

        var upstreamJson = """
                {
                  "warehouseId":"%1$s",
                  "locationName":"LocC",
                  "address":"AddrC",
                  "capacity":300
                }
                """.formatted(VALID_ID);

        mockServer.expect(once(),
                        requestTo(UPSTREAM_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(upstreamJson));

        webClient.post()
                .uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.warehouseId").isEqualTo(VALID_ID)
                .jsonPath("$._links.self.href").isNotEmpty();

        mockServer.verify();
    }

    @Test
    void createWarehouse_whenUpstream422_thenReturns422() {
        mockServer.expect(once(),
                        requestTo(UPSTREAM_BASE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"invalid capacity\"}"));

        webClient.post()
                .uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .bodyValue(WarehouseRequestModel.builder()
                        .locationName("Bad")
                        .address("A")
                        .capacity(50)
                        .build())
                .exchange()
                .expectStatus().isEqualTo(422);

        mockServer.verify();
    }

    @Test
    void updateWarehouse_whenValid_then200AndBody() throws Exception {
        var req = WarehouseRequestModel.builder()
                .locationName("LocD")
                .address("AddrD")
                .capacity(400)
                .build();

        mockServer.expect(once(),
                        requestTo(UPSTREAM_BASE + "/" + VALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withNoContent());

        var upstreamJson = """
                {
                  "warehouseId":"%1$s",
                  "locationName":"LocD",
                  "address":"AddrD",
                  "capacity":400
                }
                """.formatted(VALID_ID);

        mockServer.expect(once(),
                        requestTo(UPSTREAM_BASE + "/" + VALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(upstreamJson, MediaType.APPLICATION_JSON));

        webClient.put()
                .uri(GATEWAY_BASE + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WarehouseResponseModel.class)
                .value(w -> {
                    assertEquals(VALID_ID, w.getWarehouseId());
                    assertEquals(400, w.getCapacity());
                });

        mockServer.verify();
    }

    @Test
    void updateWarehouse_whenUpstream404_thenReturns404() {
        mockServer.expect(once(),
                        requestTo(UPSTREAM_BASE + "/" + NOTFOUND_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found\"}"));

        webClient.put()
                .uri(GATEWAY_BASE + "/" + NOTFOUND_ID)
                .accept(MediaTypes.HAL_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(WarehouseRequestModel.builder()
                        .locationName("X")
                        .address("Y")
                        .capacity(200)
                        .build())
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void deleteWarehouse_whenValid_then204NoContent() throws Exception {
        mockServer.expect(once(),
                        requestTo(UPSTREAM_BASE + "/" + VALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        webClient.delete()
                .uri(GATEWAY_BASE + "/" + VALID_ID)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isNoContent();

        mockServer.verify();
    }

    @Test
    void deleteWarehouse_whenUpstream404_thenReturns404() {
        mockServer.expect(once(),
                        requestTo(UPSTREAM_BASE + "/" + NOTFOUND_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found\"}"));

        webClient.delete()
                .uri(GATEWAY_BASE + "/" + NOTFOUND_ID)
                .accept(MediaTypes.HAL_JSON)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }
}
