package com.footballstore.apigateway.businesslayer.customers;

import com.footballstore.apigateway.domainclientlayer.customers.CustomersServiceClient;
import com.footballstore.apigateway.presentationlayer.customers.CustomerRequestModel;
import com.footballstore.apigateway.presentationlayer.customers.CustomerResponseModel;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@Service
public class CustomersServiceImpl implements CustomersService {

    private final CustomersServiceClient customersServiceClient;

    public CustomersServiceImpl(CustomersServiceClient customersServiceClient) {
        this.customersServiceClient = customersServiceClient;
    }

    @Override
    public List<CustomerResponseModel> getAllCustomers() {
        log.debug("API‑Gateway Customers Service: fetching all");
        List<CustomerResponseModel> customers = customersServiceClient.getAllCustomers();
        customers.forEach(this::enrichWithLinks);
        return customers;
    }

    @Override
    public CustomerResponseModel getCustomerById(String customerId) {
        validateUuid(customerId);
        log.debug("API‑Gateway Customers Service: fetching id={}", customerId);
        CustomerResponseModel customer = customersServiceClient.getCustomerById(customerId);
        enrichWithLinks(customer);
        return customer;
    }

    @Override
    public CustomerResponseModel createCustomer(CustomerRequestModel requestModel) {
        log.debug("API‑Gateway Customers Service: creating");
        CustomerResponseModel created = customersServiceClient.createCustomer(requestModel);
        enrichWithLinks(created);
        return created;
    }

    @Override
    public CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel requestModel) {
        validateUuid(customerId);
        log.debug("API‑Gateway Customers Service: updating id={}", customerId);
        CustomerResponseModel updated = customersServiceClient.updateCustomer(customerId, requestModel);
        enrichWithLinks(updated);
        return updated;
    }

    @Override
    public CustomerResponseModel deleteCustomer(String customerId) {
        validateUuid(customerId);
        log.debug("API‑Gateway Customers Service: deleting id={}", customerId);
        customersServiceClient.deleteCustomer(customerId);
        return null;
    }

    private void validateUuid(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Provided customerId is invalid: " + id);
        }
    }

    private void enrichWithLinks(CustomerResponseModel customer) {
        if (customer != null && customer.getCustomerId() != null) {
            customer.add(linkTo(
                    methodOn(com.footballstore.apigateway.presentationlayer.customers.CustomersController.class)
                            .getCustomerById(customer.getCustomerId()))
                    .withSelfRel());
            customer.add(linkTo(
                    methodOn(com.footballstore.apigateway.presentationlayer.customers.CustomersController.class)
                            .getAllCustomers())
                    .withRel("allCustomers"));
        }
    }
}
