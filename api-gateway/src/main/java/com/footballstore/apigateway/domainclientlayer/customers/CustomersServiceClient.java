package com.footballstore.apigateway.domainclientlayer.customers;

import com.footballstore.apigateway.presentationlayer.customers.CustomerRequestModel;
import com.footballstore.apigateway.presentationlayer.customers.CustomerResponseModel;
import com.footballstore.apigateway.utils.HttpErrorInfo;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import com.footballstore.apigateway.utils.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@Component
public class CustomersServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String CUSTOMERS_SERVICE_BASE_URL;

    public CustomersServiceClient(RestTemplate restTemplate, ObjectMapper mapper,
                                  @Value("${app.customers-service.host}") String customersServiceHost,
                                  @Value("${app.customers-service.port}") String customersServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        CUSTOMERS_SERVICE_BASE_URL = "http://" + customersServiceHost + ":" + customersServicePort + "/api/v1/customers";
    }

    public List<CustomerResponseModel> getAllCustomers() {
        log.debug("API-Gateway: Fetching all customers");
        try {
            ResponseEntity<CustomerResponseModel[]> responseEntity =
                    restTemplate.getForEntity(CUSTOMERS_SERVICE_BASE_URL, CustomerResponseModel[].class);
            CustomerResponseModel[] customersArray = responseEntity.getBody();
            return customersArray != null ? Arrays.asList(customersArray) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerResponseModel getCustomerById(String customerId) {
        log.debug("API-Gateway: Fetching customer with id: " + customerId);
        try {
            String url = CUSTOMERS_SERVICE_BASE_URL + "/" + customerId;
            return restTemplate.getForObject(url, CustomerResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerResponseModel createCustomer(CustomerRequestModel customerRequest) {
        log.debug("API-Gateway: Creating customer");
        try {
            return restTemplate.postForObject(CUSTOMERS_SERVICE_BASE_URL, customerRequest, CustomerResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel customerRequest) {
        log.debug("API-Gateway: Updating customer with id: " + customerId);
        try {
            String url = CUSTOMERS_SERVICE_BASE_URL + "/" + customerId;
            restTemplate.put(url, customerRequest);
            return getCustomerById(customerId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerResponseModel deleteCustomer(String customerId) {
        log.debug("API-Gateway: Deleting customer with id: " + customerId);
        try {
            String url = CUSTOMERS_SERVICE_BASE_URL + "/" + customerId;
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
        return null;
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            HttpErrorInfo errorInfo = mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
            return errorInfo.getMessage();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(getErrorMessage(ex));
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(getErrorMessage(ex));
        }
        log.warn("Unexpected HTTP error: " + ex.getStatusCode());
        return ex;
    }
}
