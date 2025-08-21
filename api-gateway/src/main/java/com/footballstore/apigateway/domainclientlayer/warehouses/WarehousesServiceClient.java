package com.footballstore.apigateway.domainclientlayer.warehouses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballstore.apigateway.presentationlayer.warehouses.WarehouseRequestModel;
import com.footballstore.apigateway.presentationlayer.warehouses.WarehouseResponseModel;
import com.footballstore.apigateway.utils.HttpErrorInfo;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import com.footballstore.apigateway.utils.exceptions.NotFoundException;
import com.footballstore.apigateway.utils.exceptions.InvalidWarehouseCapacityException;
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
import static org.springframework.http.HttpStatus.*;

@Slf4j
@Component
public class WarehousesServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String WAREHOUSES_SERVICE_BASE_URL;

    public WarehousesServiceClient(RestTemplate restTemplate, ObjectMapper mapper,
                                   @Value("${app.warehouses-service.host}") String warehousesServiceHost,
                                   @Value("${app.warehouses-service.port}") String warehousesServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.WAREHOUSES_SERVICE_BASE_URL = "http://" + warehousesServiceHost + ":" + warehousesServicePort + "/api/v1/warehouses";
    }

    public List<WarehouseResponseModel> getAllWarehouses() {
        log.debug("API-Gateway: Fetching all warehouses");
        try {
            ResponseEntity<WarehouseResponseModel[]> responseEntity =
                    restTemplate.getForEntity(WAREHOUSES_SERVICE_BASE_URL, WarehouseResponseModel[].class);
            WarehouseResponseModel[] array = responseEntity.getBody();
            return array != null ? Arrays.asList(array) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public WarehouseResponseModel getWarehouseById(String warehouseId) {
        log.debug("API-Gateway: Fetching warehouse with id: {}", warehouseId);
        try {
            String url = WAREHOUSES_SERVICE_BASE_URL + "/" + warehouseId;
            return restTemplate.getForObject(url, WarehouseResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public WarehouseResponseModel createWarehouse(WarehouseRequestModel request) {
        log.debug("API-Gateway: Creating warehouse");
        try {
            return restTemplate.postForObject(WAREHOUSES_SERVICE_BASE_URL, request, WarehouseResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public WarehouseResponseModel updateWarehouse(String warehouseId, WarehouseRequestModel request) {
        log.debug("API-Gateway: Updating warehouse with id: {}", warehouseId);
        try {
            String url = WAREHOUSES_SERVICE_BASE_URL + "/" + warehouseId;
            restTemplate.put(url, request);
            return getWarehouseById(warehouseId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteWarehouse(String warehouseId) {
        log.debug("API-Gateway: Deleting warehouse with id: {}", warehouseId);
        try {
            String url = WAREHOUSES_SERVICE_BASE_URL + "/" + warehouseId;
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            HttpErrorInfo errorInfo = mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class);
            return errorInfo.getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(getErrorMessage(ex));
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            String message = getErrorMessage(ex);
            if (message != null && message.toLowerCase().contains("capacity")) {
                return new InvalidWarehouseCapacityException(message);
            }
            return new InvalidInputException(message);
        }
        log.warn("Unexpected HTTP error: {}", ex.getStatusCode());
        return ex;
    }
}
