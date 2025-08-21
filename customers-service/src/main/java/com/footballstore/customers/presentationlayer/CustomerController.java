package com.footballstore.customers.presentationlayer;

import com.footballstore.customers.businesslayer.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerResponseModel>> getAllCustomers() {
        List<CustomerResponseModel> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponseModel> getCustomerById(@PathVariable String customerId) {
        CustomerResponseModel customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<CustomerResponseModel> createCustomer(@RequestBody CustomerRequestModel requestModel) {
        CustomerResponseModel created = customerService.createCustomer(requestModel);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerResponseModel> updateCustomer(@PathVariable String customerId,
                                                                @RequestBody CustomerRequestModel requestModel) {
        CustomerResponseModel updated = customerService.updateCustomer(customerId, requestModel);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}