package com.footballstore.orders.domainclientlayer.warehouses;

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
public class WarehousesServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String BASE_URL;

    public WarehousesServiceClient(RestTemplate restTemplate,
                                   ObjectMapper mapper,
                                   @Value("${app.warehouses-service.host}") String host,
                                   @Value("${app.warehouses-service.port}") String port) {
        this.restTemplate = restTemplate;
        this.mapper       = mapper;
        this.BASE_URL     = "http://" + host + ":" + port + "/api/v1/warehouses";
    }

    public List<WarehouseModel> getAllWarehouses() {
        log.debug("2. Request received in Orders-Service WarehousesServiceClient: getAllWarehouses");
        try {
            ResponseEntity<WarehouseModel[]> resp =
                    restTemplate.getForEntity(BASE_URL, WarehouseModel[].class);
            WarehouseModel[] arr = resp.getBody();
            return arr != null ? Arrays.asList(arr) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public WarehouseModel getWarehouseByWarehouseId(String warehouseId) {
        log.debug("2. Request received in Orders-Service WarehousesServiceClient: getWarehouseByWarehouseId({})", warehouseId);
        try {
            String url = BASE_URL + "/" + warehouseId;
            return restTemplate.getForObject(url, WarehouseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public WarehouseModel createWarehouse(WarehouseModel newWarehouse) {
        log.debug("2. Request received in Orders-Service WarehousesServiceClient: createWarehouse");
        try {
            return restTemplate.postForObject(BASE_URL, newWarehouse, WarehouseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public WarehouseModel updateWarehouse(String warehouseId, WarehouseModel updatedWarehouse) {
        log.debug("2. Request received in Orders-Service WarehousesServiceClient: updateWarehouse({})", warehouseId);
        try {
            String url = BASE_URL + "/" + warehouseId;
            restTemplate.put(url, updatedWarehouse);
            return getWarehouseByWarehouseId(warehouseId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteWarehouse(String warehouseId) {
        log.debug("2. Request received in Orders-Service WarehousesServiceClient: deleteWarehouse({})", warehouseId);
        try {
            String url = BASE_URL + "/" + warehouseId;
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public int getStock(String warehouseId, String apparelId) {
        String url = BASE_URL + "/" + warehouseId + "/apparels/" + apparelId + "/stock";
        return restTemplate.getForObject(url, Integer.class);
    }

    public boolean isInStock(String warehouseId, String apparelId, int qty) {
        return getStock(warehouseId, apparelId) >= qty;
    }

    public void decreaseStock(String warehouseId, String apparelId, int qty) {
        String url = BASE_URL + "/" + warehouseId + "/apparels/" + apparelId + "/stock/decrease?quantity=" + qty;
        restTemplate.patchForObject(url, null, Void.class);
    }

    public void increaseStock(String warehouseId, String apparelId, int qty) {
        String url = BASE_URL + "/" + warehouseId + "/apparels/" + apparelId + "/stock/increase?quantity=" + qty;
        restTemplate.patchForObject(url, null, Void.class);
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
        log.warn("Unexpected HTTP error from Warehouses Service: {}, body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ex;
    }
}
