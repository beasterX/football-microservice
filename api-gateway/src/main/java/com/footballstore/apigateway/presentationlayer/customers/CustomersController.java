package com.footballstore.apigateway.presentationlayer.customers;

import com.footballstore.apigateway.businesslayer.customers.CustomersService;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/customers")
public class CustomersController {

    private final CustomersService customersService;

    public CustomersController(CustomersService customersService) {
        this.customersService = customersService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CustomerResponseModel>> getAllCustomers() {
        log.debug("Gateway Controller: GET all customers");
        return ResponseEntity.ok(customersService.getAllCustomers());
    }

    @GetMapping(value = "/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerResponseModel> getCustomerById(@PathVariable String customerId) {
        log.debug("Gateway Controller: GET customer {}", customerId);
        return ResponseEntity.ok(customersService.getCustomerById(customerId));
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CustomerResponseModel> createCustomer(
            @RequestBody CustomerRequestModel requestModel
    ) {
        log.debug("Gateway Controller: POST create customer");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customersService.createCustomer(requestModel));
    }

    @PutMapping(
            value = "/{customerId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<CustomerResponseModel> updateCustomer(
            @PathVariable String customerId,
            @RequestBody CustomerRequestModel requestModel
    ) {
        log.debug("Gateway Controller: PUT update {}", customerId);
        return ResponseEntity.ok(customersService.updateCustomer(customerId, requestModel));
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        log.debug("Gateway Controller: DELETE {}", customerId);
        customersService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}
