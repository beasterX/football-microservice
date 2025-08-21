package com.footballstore.apparels.presentationlayer;

import com.footballstore.apparels.dataaccesslayer.ApparelRepository;
import com.footballstore.apparels.dataaccesslayer.ApparelType;
import com.footballstore.apparels.dataaccesslayer.SizeOption;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql({"/schema-h2.sql"})
@Sql({"/data-h2.sql"})
class ApparelControllerIntegrationTest {

    private static final String BASE_URI = "/api/v1/apparels";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ApparelRepository apparelRepository;

    @BeforeEach
    void setup() {
        apparelRepository.deleteAll();
    }

    @Test
    void getAll_empty_returns200EmptyList() {
        webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApparelResponseModel.class)
                .hasSize(0);
    }

    @Test
    void getAll_twoApparels_returns200AndListOfTwo() {
        var a1 = ApparelRequestModel.builder()
                .itemName("Jersey A")
                .description("A Jersey")
                .brand("BrandA")
                .price(new BigDecimal("59.99"))
                .cost(new BigDecimal("30.00"))
                .stock(100)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.M)
                .build();
        var a2 = ApparelRequestModel.builder()
                .itemName("Shorts B")
                .description("B Shorts")
                .brand("BrandB")
                .price(new BigDecimal("39.99"))
                .cost(new BigDecimal("20.00"))
                .stock(50)
                .apparelType(ApparelType.SHORTS)
                .sizeOption(SizeOption.S)
                .build();

        webTestClient.post().uri(BASE_URI).contentType(MediaType.APPLICATION_JSON).bodyValue(a1)
                .exchange().expectStatus().isCreated();
        webTestClient.post().uri(BASE_URI).contentType(MediaType.APPLICATION_JSON).bodyValue(a2)
                .exchange().expectStatus().isCreated();

        webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApparelResponseModel.class)
                .hasSize(2);
    }

    @Test
    void getById_existingApparel_returns200AndApparel() {
        var req = ApparelRequestModel.builder()
                .itemName("Test Jersey")
                .description("Desc for Jersey")
                .brand("BrandX")
                .price(new BigDecimal("50.00"))
                .cost(new BigDecimal("25.00"))
                .stock(100)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.M)
                .build();

        EntityExchangeResult<ApparelResponseModel> postResult =
                webTestClient.post()
                        .uri(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(req)
                        .exchange()
                        .expectStatus().isCreated()
                        .expectBody(ApparelResponseModel.class)
                        .returnResult();

        String id = postResult.getResponseBody().getApparelId();

        webTestClient.get()
                .uri(BASE_URI + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApparelResponseModel.class)
                .value(a -> {
                    assertThat(a.getApparelId()).isEqualTo(id);
                    assertThat(a.getItemName()).isEqualTo("Test Jersey");
                });
    }

    @Test
    void getById_nonExistentValidUuid_returns404NotFound() {
        webTestClient.get()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getById_invalidUuid_returns422UnprocessableEntity() {
        webTestClient.get()
                .uri(BASE_URI + "/NOT‑UUID")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void create_validApparel_returns201AndApparel() {
        var req = ApparelRequestModel.builder()
                .itemName("New Jersey")
                .description("New Jersey Description")
                .brand("BrandY")
                .price(new BigDecimal("70.00"))
                .cost(new BigDecimal("40.00"))
                .stock(200)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.L)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ApparelResponseModel.class)
                .value(a -> {
                    assertThat(a.getApparelId()).isNotNull();
                    assertThat(a.getItemName()).isEqualTo("New Jersey");
                    assertThat(a.getBrand()).isEqualTo("BrandY");
                });
    }

    @Test
    void create_invalidPricing_returns422UnprocessableEntity() {
        var req = ApparelRequestModel.builder()
                .itemName("Invalid Pricing Jersey")
                .description("Desc for invalid pricing")
                .brand("BrandZ")
                .price(new BigDecimal("50.00"))
                .cost(new BigDecimal("60.00"))
                .stock(150)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.M)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void update_existingApparel_returns200AndUpdatedApparel() {
        var createReq = ApparelRequestModel.builder()
                .itemName("Old Jersey")
                .description("Old Description")
                .brand("BrandX")
                .price(new BigDecimal("60.00"))
                .cost(new BigDecimal("35.00"))
                .stock(100)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.M)
                .build();

        EntityExchangeResult<ApparelResponseModel> postResult =
                webTestClient.post()
                        .uri(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(createReq)
                        .exchange()
                        .expectStatus().isCreated()
                        .expectBody(ApparelResponseModel.class)
                        .returnResult();

        String id = postResult.getResponseBody().getApparelId();

        var updateReq = ApparelRequestModel.builder()
                .itemName("Updated Jersey")
                .description("Updated Description")
                .brand("BrandX")
                .price(new BigDecimal("65.00"))
                .cost(new BigDecimal("30.00"))
                .stock(120)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.M)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApparelResponseModel.class)
                .value(a -> {
                    assertThat(a.getApparelId()).isEqualTo(id);
                    assertThat(a.getStock()).isEqualTo(120);
                });
    }

    @Test
    void update_nonExistentValidUuid_returns404NotFound() {
        var req = ApparelRequestModel.builder()
                .itemName("Nope")
                .description("Does not exist")
                .brand("BrandX")
                .price(new BigDecimal("65.00"))
                .cost(new BigDecimal("30.00"))
                .stock(120)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.M)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void update_invalidUuid_returns422UnprocessableEntity() {
        var req = ApparelRequestModel.builder()
                .itemName("Bad")
                .description("Bad")
                .brand("BrandX")
                .price(new BigDecimal("65.00"))
                .cost(new BigDecimal("30.00"))
                .stock(10)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.S)
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/BAD‑ID")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void delete_existingApparel_returns204NoContent() {
        var createReq = ApparelRequestModel.builder()
                .itemName("To Delete Jersey")
                .description("Desc")
                .brand("BrandY")
                .price(new BigDecimal("55.00"))
                .cost(new BigDecimal("30.00"))
                .stock(90)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.S)
                .build();

        EntityExchangeResult<ApparelResponseModel> postResult =
                webTestClient.post()
                        .uri(BASE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(createReq)
                        .exchange()
                        .expectStatus().isCreated()
                        .expectBody(ApparelResponseModel.class)
                        .returnResult();

        String id = postResult.getResponseBody().getApparelId();

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
    void delete_nonExistentValidUuid_returns404NotFound() {
        webTestClient.delete()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void delete_invalidUuid_returns422UnprocessableEntity() {
        webTestClient.delete()
                .uri(BASE_URI + "/BAD‑ID")
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void create_validPricing_noError_returns201Created() {
        var req = ApparelRequestModel.builder()
                .itemName("Valid Pricing Jersey")
                .description("Jersey with valid pricing")
                .brand("BrandY")
                .price(new BigDecimal("70.00"))
                .cost(new BigDecimal("40.00"))
                .stock(100)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.L)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void create_invalidPricing_throwsInvalidApparelPricingException_returns422() {
        var req = ApparelRequestModel.builder()
                .itemName("Invalid Pricing Jersey")
                .description("Jersey with pricing error")
                .brand("BrandZ")
                .price(new BigDecimal("50.00"))
                .cost(new BigDecimal("60.00"))
                .stock(150)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.M)
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);
    }
}
