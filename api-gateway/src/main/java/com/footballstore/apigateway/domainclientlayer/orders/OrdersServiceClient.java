package com.footballstore.apigateway.domainclientlayer.orders;

import com.footballstore.apigateway.presentationlayer.orders.OrderRequestModel;
import com.footballstore.apigateway.presentationlayer.orders.OrderResponseModel;
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
public class OrdersServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String ORDERS_SERVICE_BASE_URL;

    public OrdersServiceClient(RestTemplate restTemplate,
                               ObjectMapper mapper,
                               @Value("${app.orders-service.host}") String host,
                               @Value("${app.orders-service.port}") String port) {

        this.restTemplate = restTemplate;
        this.mapper       = mapper;
        this.ORDERS_SERVICE_BASE_URL =
                "http://" + host + ":" + port + "/api/v1/customers";
    }

    public List<OrderResponseModel> getAllCustomerOrders(String customerId) {
        log.debug("API-Gateway: Fetching all orders for customerId={}", customerId);
        try {
            String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders";
            ResponseEntity<OrderResponseModel[]> resp =
                    restTemplate.getForEntity(url, OrderResponseModel[].class);
            OrderResponseModel[] arr = resp.getBody();
            return arr != null ? Arrays.asList(arr) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public OrderResponseModel getCustomerOrderById(String customerId, String orderId) {
        log.debug("API-Gateway: Fetching order {} for customer {}", orderId, customerId);
        try {
            String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders/" + orderId;
            return restTemplate.getForObject(url, OrderResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public OrderResponseModel processCustomerOrder(String customerId, OrderRequestModel request) {
        log.debug("API-Gateway: Creating order for customer {}", customerId);
        try {
            String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders";
            return restTemplate.postForObject(url, request, OrderResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public OrderResponseModel updateCustomerOrder(String customerId,
                                                  String orderId,
                                                  OrderRequestModel request) {
        log.debug("API-Gateway: Updating order {} for customer {}", orderId, customerId);
        try {
            String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders/" + orderId;
            restTemplate.put(url, request);
            return getCustomerOrderById(customerId, orderId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteCustomerOrder(String customerId, String orderId) {
        log.debug("API-Gateway: Deleting order {} for customer {}", orderId, customerId);
        try {
            String url = ORDERS_SERVICE_BASE_URL + "/" + customerId + "/orders/" + orderId;
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private String extractErrorMessage(HttpClientErrorException ex) {
        try {
            HttpErrorInfo info = mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
            return info.getMessage();
        } catch (IOException ioe) {
            return ex.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        String msg = extractErrorMessage(ex);
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(msg);
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(msg);
        }
        log.warn("Unexpected HTTP error from Orders Service: {} – rethrowing", ex.getStatusCode());
        return ex;
    }
}
