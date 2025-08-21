package com.footballstore.warehouses.presentationlayer;

import com.footballstore.warehouses.dataaccesslayer.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql({"/schema-h2.sql"})
@Sql({"/data-h2.sql"})
class WarehouseControllerIntegrationTest {

    private static final String BASE_URI = "/api/v1/warehouses";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @BeforeEach
    void setup() {
        warehouseRepository.deleteAll();
    }

    @Test
    void getAll_empty_returns200EmptyList() {
        webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WarehouseResponseModel.class)
                .hasSize(0);
    }

    @Test
    void getAll_twoWarehouses_returns200AndListOfTwo() {
        var wh1 = WarehouseRequestModel.builder()
                .locationName("Central Warehouse")
                .address("123 Warehouse Ave")
                .capacity(500)
                .build();
        var wh2 = WarehouseRequestModel.builder()
                .locationName("North Depot")
                .address("456 North St")
                .capacity(300)
                .build();

        webTestClient.post().uri(BASE_URI).contentType(MediaType.APPLICATION_JSON).bodyValue(wh1)
                .exchange().expectStatus().isCreated();
        webTestClient.post().uri(BASE_URI).contentType(MediaType.APPLICATION_JSON).bodyValue(wh2)
                .exchange().expectStatus().isCreated();

        webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(WarehouseResponseModel.class)
                .hasSize(2);
    }

    @Test
    void getById_existingWarehouse_returns200AndWarehouse() {
        var req = WarehouseRequestModel.builder()
                .locationName("Test Warehouse")
                .address("Test Address")
                .capacity(300)
                .build();

        EntityExchangeResult<WarehouseResponseModel> postResult =
                webTestClient.post()
                        .uri(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(req)
                        .exchange()
                        .expectStatus().isCreated()
                        .expectBody(WarehouseResponseModel.class)
                        .returnResult();

        String id = postResult.getResponseBody().getWarehouseId();

        webTestClient.get()
                .uri(BASE_URI + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WarehouseResponseModel.class)
                .value(w -> {
                    assertThat(w.getWarehouseId()).isEqualTo(id);
                    assertThat(w.getLocationName()).isEqualTo("Test Warehouse");
                });
    }

    @Test
    void getById_unknownWarehouse_returns404NotFound() {
        webTestClient.get()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getById_invalidWarehouseId_returns422UnprocessableEntity() {
        webTestClient.get()
                .uri(BASE_URI + "/INVALID‑ID")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void create_validWarehouse_returns201AndWarehouse() {
        var req = WarehouseRequestModel.builder()
                .locationName("Central Test")
                .address("Center Street")
                .capacity(500)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(WarehouseResponseModel.class)
                .value(w -> {
                    assertThat(w.getWarehouseId()).isNotNull();
                    assertThat(w.getLocationName()).isEqualTo("Central Test");
                    assertThat(w.getCapacity()).isEqualTo(500);
                });
    }

    @Test
    void create_tooLowCapacity_returns422UnprocessableEntity() {
        var req = WarehouseRequestModel.builder()
                .locationName("Tiny")
                .address("Addr")
                .capacity(50)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void update_existingWarehouse_returns200AndUpdatedWarehouse() {
        var createReq = WarehouseRequestModel.builder()
                .locationName("Before Update")
                .address("Old Addr")
                .capacity(400)
                .build();

        EntityExchangeResult<WarehouseResponseModel> postResult =
                webTestClient.post()
                        .uri(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(createReq)
                        .exchange()
                        .expectStatus().isCreated()
                        .expectBody(WarehouseResponseModel.class)
                        .returnResult();

        String id = postResult.getResponseBody().getWarehouseId();

        var updateReq = WarehouseRequestModel.builder()
                .locationName("After Update")
                .address("New Addr")
                .capacity(600)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WarehouseResponseModel.class)
                .value(w -> {
                    assertThat(w.getWarehouseId()).isEqualTo(id);
                    assertThat(w.getCapacity()).isEqualTo(600);
                });
    }

    @Test
    void update_unknownWarehouse_returns404NotFound() {
        var req = WarehouseRequestModel.builder()
                .locationName("Nope")
                .address("Nowhere")
                .capacity(200)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void update_invalidWarehouseId_returns422UnprocessableEntity() {
        var req = WarehouseRequestModel.builder()
                .locationName("Bad")
                .address("Bad")
                .capacity(150)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/BAD‑ID")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void delete_existingWarehouse_returns204NoContent() {
        var createReq = WarehouseRequestModel.builder()
                .locationName("To Delete")
                .address("Addr")
                .capacity(300)
                .build();

        EntityExchangeResult<WarehouseResponseModel> postResult =
                webTestClient.post()
                        .uri(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(createReq)
                        .exchange()
                        .expectStatus().isCreated()
                        .expectBody(WarehouseResponseModel.class)
                        .returnResult();

        String id = postResult.getResponseBody().getWarehouseId();

        webTestClient.delete()
                .uri(BASE_URI + "/" + id)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(BASE_URI + "/" + id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void delete_unknownWarehouse_returns404NotFound() {
        webTestClient.delete()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void delete_invalidWarehouseId_returns422UnprocessableEntity() {
        webTestClient.delete()
                .uri(BASE_URI + "/BAD‑ID")
                .exchange()
                .expectStatus().isEqualTo(422);
    }


    @Test
    void create_validCapacity_noError_returns201Created() {
        var req = WarehouseRequestModel.builder()
                .locationName("Valid")
                .address("Addr")
                .capacity(150)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void create_invalidCapacity_throwsInvalidWarehouseCapacityException_returns422() {
        var req = WarehouseRequestModel.builder()
                .locationName("Tiny")
                .address("Addr")
                .capacity(50)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);
    }
}
