package com.footballstore.apigateway.presentationlayer.customers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.footballstore.apigateway.businesslayer.customers.CustomersService;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import com.footballstore.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class CustomersControllerUnitTest {

    @Autowired
    private CustomersController customersController;

    @MockitoBean
    private CustomersService customersService;

    private final String VALID_ID   = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private final String NOT_FOUND  = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private final String BAD_ID     = "bad-id-format";

    @Test
    void getAllCustomers_whenServiceReturnsList_thenOkAndBody() {
        var c1 = CustomerResponseModel.builder()
                .customerId("id1").firstName("A").lastName("B")
                .email("a@b.com").phone("123").build();
        var c2 = CustomerResponseModel.builder()
                .customerId("id2").firstName("C").lastName("D")
                .email("c@d.com").phone("456").build();

        when(customersService.getAllCustomers()).thenReturn(List.of(c1, c2));

        ResponseEntity<List<CustomerResponseModel>> resp = customersController.getAllCustomers();

        assertAll("getAll",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals(2, resp.getBody().size()),
                () -> assertEquals("id1", resp.getBody().get(0).getCustomerId())
        );
        verify(customersService, times(1)).getAllCustomers();
    }

    @Test
    void getCustomerById_whenValid_thenOkAndBody() {
        var model = CustomerResponseModel.builder()
                .customerId(VALID_ID)
                .firstName("X").lastName("Y")
                .email("x@y.com").phone("789").build();

        when(customersService.getCustomerById(VALID_ID)).thenReturn(model);

        ResponseEntity<CustomerResponseModel> resp =
                customersController.getCustomerById(VALID_ID);

        assertAll("getById",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals("X", resp.getBody().getFirstName()),
                () -> assertEquals("789", resp.getBody().getPhone())
        );
        verify(customersService, times(1)).getCustomerById(VALID_ID);
    }

    @Test
    void getCustomerById_whenServiceThrowsNotFound_thenPropagates() {
        doThrow(new NotFoundException("not found")).when(customersService).getCustomerById(NOT_FOUND);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                customersController.getCustomerById(NOT_FOUND)
        );
        assertEquals("not found", ex.getMessage());
        verify(customersService).getCustomerById(NOT_FOUND);
    }

    @Test
    void createCustomer_whenValid_thenReturnsCreatedAndBody() {
        var req = CustomerRequestModel.builder()
                .firstName("New").lastName("User")
                .email("new@u.com").phone("000")
                .street("S").city("C").state("ST")
                .postalCode("PC").country("CT")
                .build();
        var respModel = CustomerResponseModel.builder()
                .customerId(VALID_ID)
                .firstName("New").lastName("User")
                .email("new@u.com").phone("000")
                .build();

        when(customersService.createCustomer(req)).thenReturn(respModel);

        ResponseEntity<CustomerResponseModel> resp =
                customersController.createCustomer(req);

        assertAll("create",
                () -> assertEquals(HttpStatus.CREATED, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals(VALID_ID, resp.getBody().getCustomerId()),
                () -> assertEquals("new@u.com", resp.getBody().getEmail())
        );
        verify(customersService).createCustomer(req);
    }

    @Test
    void createCustomer_whenServiceThrowsInvalidInput_thenPropagates() {
        var req = CustomerRequestModel.builder().build();
        doThrow(new InvalidInputException("bad"))
                .when(customersService).createCustomer(req);

        InvalidInputException ex = assertThrows(InvalidInputException.class, () ->
                customersController.createCustomer(req)
        );
        assertEquals("bad", ex.getMessage());
        verify(customersService).createCustomer(req);
    }

    @Test
    void updateCustomer_whenValid_thenOkAndBody() {
        var req = CustomerRequestModel.builder()
                .firstName("Up").lastName("D")
                .email("up@d.com").phone("111")
                .street("S").city("C").state("ST")
                .postalCode("PC").country("CT")
                .build();
        var respModel = CustomerResponseModel.builder()
                .customerId(VALID_ID)
                .firstName("Up").lastName("D")
                .email("up@d.com").phone("111")
                .build();

        when(customersService.updateCustomer(VALID_ID, req)).thenReturn(respModel);

        ResponseEntity<CustomerResponseModel> resp =
                customersController.updateCustomer(VALID_ID, req);

        assertAll("update",
                () -> assertEquals(HttpStatus.OK, resp.getStatusCode()),
                () -> assertNotNull(resp.getBody()),
                () -> assertEquals("Up", resp.getBody().getFirstName()),
                () -> assertEquals("111", resp.getBody().getPhone())
        );
        verify(customersService).updateCustomer(VALID_ID, req);
    }

    @Test
    void updateCustomer_whenServiceThrowsNotFound_thenPropagates() {
        var req = CustomerRequestModel.builder().build();
        doThrow(new NotFoundException("nope"))
                .when(customersService).updateCustomer(NOT_FOUND, req);

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                customersController.updateCustomer(NOT_FOUND, req)
        );
        assertEquals("nope", ex.getMessage());
        verify(customersService).updateCustomer(NOT_FOUND, req);
    }

    @Test
    void deleteCustomer_whenValid_thenNoContent() {
        when(customersService.deleteCustomer(VALID_ID)).thenReturn(null);

        ResponseEntity<Void> resp =
                customersController.deleteCustomer(VALID_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(customersService, times(1)).deleteCustomer(VALID_ID);
    }

    @Test
    void deleteCustomer_whenServiceThrowsNotFound_thenPropagates() {
        when(customersService.deleteCustomer(NOT_FOUND))
                .thenThrow(new NotFoundException("gone"));

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> customersController.deleteCustomer(NOT_FOUND)
        );
        assertEquals("gone", ex.getMessage());
        verify(customersService, times(1)).deleteCustomer(NOT_FOUND);
    }
}