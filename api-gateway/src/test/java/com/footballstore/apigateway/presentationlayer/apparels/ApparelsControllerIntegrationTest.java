package com.footballstore.apigateway.presentationlayer.apparels;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.apparels-service.host=localhost",
        "app.apparels-service.port=7003"
})
class ApparelsControllerIntegrationTest {

    private static final String GATEWAY_BASE = "/api/v1/apparels";
    private static final String UPSTREAM_BASE = "http://localhost:7003/api/v1/apparels";

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
    void getAllApparels_whenUpstreamReturnsList_thenGatewayReturns200AndBody() throws Exception {
        var upstreamJson = """
        [
          {
            "apparelId":"aaa11111-bbbb-cccc-dddd-eeeeeeeeeeee",
            "itemName":"Jersey A",
            "description":"A cool jersey",
            "brand":"BrandA",
            "price":59.99,
            "cost":30.00,
            "stock":100,
            "apparelType":"JERSEY",
            "sizeOption":"M"
          },
          {
            "apparelId":"aaa22222-bbbb-cccc-dddd-eeeeeeeeeeee",
            "itemName":"Shorts B",
            "description":"A cool short",
            "brand":"BrandB",
            "price":39.99,
            "cost":20.00,
            "stock":50,
            "apparelType":"SHORTS",
            "sizeOption":"S"
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
                .expectBodyList(ApparelResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    assertEquals(2, list.size());
                    var a1 = list.get(0);
                    assertAll("first apparel",
                            () -> assertEquals("aaa11111-bbbb-cccc-dddd-eeeeeeeeeeee", a1.getApparelId()),
                            () -> assertEquals("Jersey A", a1.getItemName()),
                            () -> assertEquals("BrandA", a1.getBrand()),
                            () -> assertEquals(100, a1.getStock())
                    );
                });

        mockServer.verify();
    }

    @Test
    void getAllApparels_whenUpstreamNotFound_thenGatewayReturns404() throws Exception {
        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE)))
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
    void getApparelById_whenValid_thenReturns200AndApparel() throws Exception {
        String id = "aaa11111-bbbb-cccc-dddd-eeeeeeeeeeee";
        var upstreamJson = """
        {
          "apparelId":"%s",
          "itemName":"Jersey A",
          "description":"A cool jersey",
          "brand":"BrandA",
          "price":59.99,
          "cost":30.00,
          "stock":100,
          "apparelType":"JERSEY",
          "sizeOption":"M"
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
                .expectBody()
                .jsonPath("$.apparelId").isEqualTo(id)
                .jsonPath("$.itemName").isEqualTo("Jersey A")
                .jsonPath("$.brand").isEqualTo("BrandA")
                .jsonPath("$.price").isEqualTo(59.99)
                .jsonPath("$.stock").isEqualTo(100)
                .jsonPath("$._links.self.href")
                .value(href -> assertTrue(((String) href).endsWith("/api/v1/apparels/" + id)))
        ;

        mockServer.verify();
    }

    @Test
    void getApparelById_whenInvalidUuid_thenReturns422() {
        webTestClient.get()
                .uri(GATEWAY_BASE + "/NOT-UUID")
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void getApparelById_whenUpstream404_thenReturns404() throws Exception {
        String id = "bbb22222-bbbb-cccc-dddd-eeeeeeeeeeee";
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
    void createApparel_whenValid_thenReturns201AndAllFields() throws Exception {
        var req = ApparelRequestModel.builder()
                .itemName("Jersey X")
                .description("Desc")
                .brand("BrandX")
                .price(new BigDecimal("70.00"))
                .cost(new BigDecimal("40.00"))
                .stock(200)
                .apparelType("JERSEY")
                .sizeOption("L")
                .build();

        var upstreamResp = """
        {
          "apparelId":"ccc33333-bbbb-cccc-dddd-eeeeeeeeeeee",
          "itemName":"Jersey X",
          "description":"Desc",
          "brand":"BrandX",
          "price":70.00,
          "cost":40.00,
          "stock":200,
          "apparelType":"JERSEY",
          "sizeOption":"L"
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
                .expectBody(ApparelResponseModel.class)
                .value(a -> {
                    assertAll("created",
                            () -> assertNotNull(a.getApparelId()),
                            () -> assertEquals("Jersey X", a.getItemName()),
                            () -> assertEquals("BrandX", a.getBrand()),
                            () -> assertEquals(200, a.getStock())
                    );
                });

        mockServer.verify();
    }

    @Test
    void createApparel_whenUpstream422_thenReturns422() throws Exception {
        var req = ApparelRequestModel.builder()
                .itemName("Bad")
                .description("Bad")
                .brand("BrandZ")
                .price(new BigDecimal("50.00"))
                .cost(new BigDecimal("60.00"))
                .stock(150)
                .apparelType("JERSEY")
                .sizeOption("M")
                .build();

        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE)))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"invalid input upstream\"}"));

        webTestClient.post()
                .uri(GATEWAY_BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);

        mockServer.verify();
    }

    @Test
    void updateApparel_whenValid_thenReturns200AndUpdated() throws Exception {
        String id = "ddd44444-bbbb-cccc-dddd-eeeeeeeeeeee";
        var req = ApparelRequestModel.builder()
                .itemName("Updated")
                .description("DescU")
                .brand("BrandU")
                .price(new BigDecimal("65.00"))
                .cost(new BigDecimal("30.00"))
                .stock(120)
                .apparelType("JERSEY")
                .sizeOption("M")
                .build();

        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withNoContent());

        var updatedJson = """
        {
          "apparelId":"%s",
          "itemName":"Updated",
          "description":"DescU",
          "brand":"BrandU",
          "price":65.00,
          "cost":30.00,
          "stock":120,
          "apparelType":"JERSEY",
          "sizeOption":"M"
        }
        """.formatted(id);

        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(updatedJson, MediaType.APPLICATION_JSON));

        webTestClient.put()
                .uri(GATEWAY_BASE + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApparelResponseModel.class)
                .value(a -> {
                    assertAll("updated",
                            () -> assertEquals(id, a.getApparelId()),
                            () -> assertEquals("Updated", a.getItemName()),
                            () -> assertEquals(120, a.getStock())
                    );
                });

        mockServer.verify();
    }

    @Test
    void updateApparel_whenUpstream404_thenReturns404() throws Exception {
        String id = "eee55555-bbbb-cccc-dddd-eeeeeeeeeeee";
        var req = ApparelRequestModel.builder()
                .itemName("X")
                .description("Y")
                .brand("Z")
                .price(new BigDecimal("10.00"))
                .cost(new BigDecimal("5.00"))
                .stock(10)
                .apparelType("SHORTS")
                .sizeOption("S")
                .build();

        mockServer.expect(once(), requestTo(new URI(UPSTREAM_BASE + "/" + id)))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"not found upstream\"}"));

        webTestClient.put()
                .uri(GATEWAY_BASE + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void deleteApparel_whenValid_thenReturns204() throws Exception {
        String id = "fff66666-bbbb-cccc-dddd-eeeeeeeeeeee";
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
    void deleteApparel_whenUpstream404_thenReturns404() throws Exception {
        String id = "bbb22222-bbbb-cccc-dddd-eeeeeeeeeeee";
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
