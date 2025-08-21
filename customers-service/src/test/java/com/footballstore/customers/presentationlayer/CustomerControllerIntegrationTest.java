package com.footballstore.customers.presentationlayer;

import com.footballstore.customers.dataaccesslayer.CustomerRepository;
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
class CustomerControllerIntegrationTest {

    private static final String BASE_URI = "/api/v1/customers";

    @Autowired private WebTestClient webTestClient;
    @Autowired private CustomerRepository customerRepository;

    @BeforeEach
    void setup() {
        customerRepository.deleteAll();
    }

    @Test
    void getAll_emptyDatabase_returns200AndEmptyList() {
        webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponseModel.class)
                .hasSize(0);
    }

    @Test
    void getAll_afterInserts_returns200AndTwoCustomers() {
        var c1 = CustomerRequestModel.builder()
                .firstName("John").lastName("Doe")
                .email("john.doe@example.com").phone("1112223333")
                .street("123 Main St").city("CityA").state("StateA")
                .postalCode("12345").country("CountryA")
                .build();
        var c2 = CustomerRequestModel.builder()
                .firstName("Jane").lastName("Doe")
                .email("jane.doe@example.com").phone("4445556666")
                .street("456 Secondary St").city("CityB").state("StateB")
                .postalCode("67890").country("CountryB")
                .build();

        webTestClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(c1).exchange().expectStatus().isCreated();
        webTestClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(c2).exchange().expectStatus().isCreated();

        webTestClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponseModel.class)
                .hasSize(2);
    }

    @Test
    void getById_existingCustomer_returns200AndCustomer() {
        var req = CustomerRequestModel.builder()
                .firstName("Alice").lastName("Smith")
                .email("alice.smith@example.com").phone("9998887777")
                .street("789 Tertiary Rd").city("CityC").state("StateC")
                .postalCode("54321").country("CountryC")
                .build();

        EntityExchangeResult<CustomerResponseModel> post = webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponseModel.class)
                .returnResult();

        String id = post.getResponseBody().getCustomerId();

        webTestClient.get()
                .uri(BASE_URI + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(body -> assertThat(body.getCustomerId()).isEqualTo(id));
    }

    @Test
    void getById_nonExistentCustomer_returns404() {
        webTestClient.get()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getById_invalidFormat_returns422() {
        webTestClient.get()
                .uri(BASE_URI + "/NOT‑A‑UUID")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void create_validCustomer_returns201AndCustomer() {
        var req = CustomerRequestModel.builder()
                .firstName("Robert").lastName("Johnson")
                .email("robert.johnson@example.com").phone("1231231234")
                .street("321 New St").city("CityD").state("StateD")
                .postalCode("11223").country("CountryD")
                .build();

        webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponseModel.class)
                .value(body -> {
                    assertThat(body.getCustomerId()).isNotNull();
                    assertThat(body.getEmail()).isEqualTo("robert.johnson@example.com");
                });
    }

    @Test
    void create_duplicateEmail_returns422UnprocessableEntity() {
        var req = CustomerRequestModel.builder()
                .firstName("Robert").lastName("Johnson")
                .email("robert.johnson@example.com").phone("1231231234")
                .street("321 New St").city("CityD").state("StateD")
                .postalCode("11223").country("CountryD")
                .build();

        webTestClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange().expectStatus().isCreated();

        webTestClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange().expectStatus().isEqualTo(422);
    }

    @Test
    void update_existingCustomer_returns200AndUpdatedCustomer() {
        var create = CustomerRequestModel.builder()
                .firstName("Emily").lastName("Clark")
                .email("emily.clark@example.com").phone("3213214321")
                .street("456 Old St").city("OldCity").state("OldState")
                .postalCode("33445").country("OldCountry")
                .build();

        EntityExchangeResult<CustomerResponseModel> post = webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(create)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponseModel.class)
                .returnResult();

        String id = post.getResponseBody().getCustomerId();

        var update = CustomerRequestModel.builder()
                .firstName("UpdatedEmily").lastName("UpdatedClark")
                .email("updated.emily@example.com").phone("9998887777")
                .street("456 Updated St").city("UpdatedCity").state("UpdatedState")
                .postalCode("77889").country("UpdatedCountry")
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseModel.class)
                .value(body -> {
                    assertThat(body.getCustomerId()).isEqualTo(id);
                    assertThat(body.getFirstName()).isEqualTo("UpdatedEmily");
                });
    }

    @Test
    void update_nonExistentCustomer_returns404() {
        var req = CustomerRequestModel.builder()
                .firstName("NonExistent").lastName("User")
                .email("nonexistent@example.com").phone("0000000000")
                .street("No St").city("NoCity").state("NoState")
                .postalCode("00000").country("NoCountry")
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void update_invalidFormat_returns422() {
        var req = CustomerRequestModel.builder()
                .firstName("X").lastName("Y")
                .email("x@y.com").phone("0")
                .street("S").city("C").state("ST")
                .postalCode("P").country("Z")
                .build();

        webTestClient.put()
                .uri(BASE_URI + "/BAD‑FORMAT")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(422);
    }

    @Test
    void delete_existingCustomer_returns204NoContent() {
        var create = CustomerRequestModel.builder()
                .firstName("Mark").lastName("Twain")
                .email("mark.twain@example.com").phone("7778889999")
                .street("789 Delete St").city("DeleteCity").state("DeleteState")
                .postalCode("11111").country("DeleteCountry")
                .build();

        EntityExchangeResult<CustomerResponseModel> post = webTestClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(create)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponseModel.class)
                .returnResult();

        String id = post.getResponseBody().getCustomerId();

        webTestClient.delete()
                .uri(BASE_URI + "/" + id)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(BASE_URI + "/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void delete_nonExistentCustomer_returns404() {
        webTestClient.delete()
                .uri(BASE_URI + "/00000000-0000-0000-0000-000000000000")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void delete_invalidFormat_returns422() {
        webTestClient.delete()
                .uri(BASE_URI + "/NOT‑UUID")
                .exchange()
                .expectStatus().isEqualTo(422);
    }
}
