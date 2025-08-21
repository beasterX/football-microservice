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
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql({"/schema-h2.sql"})
@Sql({"/data-h2.sql"})
class ApparelStockControllerIntegrationTest {

    private static final String BASE_URI = "/api/v1/apparels";
    private static final String STOCK_PATH = "/stock";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ApparelRepository apparelRepository;

    private String apparelId;

    @BeforeEach
    void setup() {
        apparelRepository.deleteAll();

        var createReq = ApparelRequestModel.builder()
                .itemName("StockTest Jersey")
                .description("Desc")
                .brand("BrandStock")
                .price(new BigDecimal("100.00"))
                .cost(new BigDecimal("50.00"))
                .stock(10)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.L)
                .build();

        apparelId = webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createReq)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ApparelResponseModel.class)
                .returnResult()
                .getResponseBody()
                .getApparelId();
    }

    @Test
    void getStock_existingApparel_returns200AndStock() {
        webTestClient.get()
                .uri(BASE_URI + "/" + apparelId + STOCK_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .value(stock -> assertThat(stock).isEqualTo(10));
    }

    @Test
    void getStock_nonExistentValidUuid_returns404NotFound() {
        webTestClient.get()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000" + STOCK_PATH)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getStock_invalidUuid_returns422UnprocessableEntity() {
        webTestClient.get()
                .uri(BASE_URI + "/BAD-ID" + STOCK_PATH)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void decreaseStock_validQuantity_decreasesAndReturns200() {
        webTestClient.patch()
                .uri(BASE_URI + "/" + apparelId + STOCK_PATH + "/decrease?quantity=3")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri(BASE_URI + "/" + apparelId + STOCK_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .value(stock -> assertThat(stock).isEqualTo(7));
    }

    @Test
    void decreaseStock_insufficientQuantity_returns422UnprocessableEntity() {
        webTestClient.patch()
                .uri(BASE_URI + "/" + apparelId + STOCK_PATH + "/decrease?quantity=20")
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void decreaseStock_invalidUuid_returns422UnprocessableEntity() {
        webTestClient.patch()
                .uri(BASE_URI + "/BAD-ID" + STOCK_PATH + "/decrease?quantity=1")
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void increaseStock_validQuantity_increasesAndReturns200() {
        webTestClient.patch()
                .uri(BASE_URI + "/" + apparelId + STOCK_PATH + "/increase?quantity=5")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri(BASE_URI + "/" + apparelId + STOCK_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Integer.class)
                .value(stock -> assertThat(stock).isEqualTo(15));
    }

    @Test
    void increaseStock_invalidUuid_returns422UnprocessableEntity() {
        webTestClient.patch()
                .uri(BASE_URI + "/BAD-ID" + STOCK_PATH + "/increase?quantity=1")
                .exchange()
                .expectStatus().isEqualTo(422);
    }
}
