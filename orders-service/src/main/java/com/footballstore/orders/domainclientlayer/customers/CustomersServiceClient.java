package com.footballstore.orders.domainclientlayer.customers;

import com.footballstore.orders.utils.HttpErrorInfo;
import com.footballstore.orders.utils.exceptions.InvalidInputException;
import com.footballstore.orders.utils.exceptions.NotFoundException;
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
    private final String BASE_URL;

    public CustomersServiceClient(RestTemplate restTemplate,
                                  ObjectMapper mapper,
                                  @Value("${app.customers-service.host}") String host,
                                  @Value("${app.customers-service.port}") String port) {
        this.restTemplate = restTemplate;
        this.mapper       = mapper;
        this.BASE_URL     = "http://" + host + ":" + port + "/api/v1/customers";
    }

    public List<CustomerModel> getAllCustomers() {
        log.debug("2. Request received in Orders-Service CustomersServiceClient: getAllCustomers");
        try {
            ResponseEntity<CustomerModel[]> resp =
                    restTemplate.getForEntity(BASE_URL, CustomerModel[].class);
            CustomerModel[] arr = resp.getBody();
            return arr != null ? Arrays.asList(arr) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerModel getCustomerByCustomerId(String customerId) {
        log.debug("2. Request received in Orders-Service CustomersServiceClient: getCustomerByCustomerId({})", customerId);
        try {
            String url = BASE_URL + "/" + customerId;
            return restTemplate.getForObject(url, CustomerModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerModel createCustomer(CustomerModel newCustomer) {
        log.debug("2. Request received in Orders-Service CustomersServiceClient: createCustomer");
        try {
            return restTemplate.postForObject(BASE_URL, newCustomer, CustomerModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public CustomerModel updateCustomer(String customerId, CustomerModel updatedCustomer) {
        log.debug("2. Request received in Orders-Service CustomersServiceClient: updateCustomer({})", customerId);
        try {
            String url = BASE_URL + "/" + customerId;
            restTemplate.put(url, updatedCustomer);
            return getCustomerByCustomerId(customerId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteCustomer(String customerId) {
        log.debug("2. Request received in Orders-Service CustomersServiceClient: deleteCustomer({})", customerId);
        try {
            String url = BASE_URL + "/" + customerId;
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        String msg;
        try {
            msg = mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException e) {
            msg = ex.getMessage();
        }
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(msg);
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(msg);
        }
        log.warn("Unexpected HTTP error from Customer Service: {}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ex;
    }
}
