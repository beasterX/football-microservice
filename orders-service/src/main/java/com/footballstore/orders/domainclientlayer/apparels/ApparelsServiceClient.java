package com.footballstore.orders.domainclientlayer.apparels;

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

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Component
public class ApparelsServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;
    private final String BASE_URL;

    public ApparelsServiceClient(RestTemplate restTemplate,
                                 ObjectMapper mapper,
                                 @Value("${app.apparels-service.host}") String host,
                                 @Value("${app.apparels-service.port}") String port) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.BASE_URL = "http://" + host + ":" + port + "/api/v1/apparels";
    }


    public List<ApparelModel> getAllApparels() {
        try {
            ResponseEntity<ApparelModel[]> resp =
                    restTemplate.getForEntity(BASE_URL, ApparelModel[].class);

            ApparelModel[] arr = resp.getBody();
            return arr != null
                    ? Arrays.asList(arr)
                    : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            throw handleException(ex);
        }
    }

    public ApparelModel getApparelByApparelId(String apparelId) {
        try {
            String url = BASE_URL + "/" + apparelId;
            return restTemplate.getForObject(url, ApparelModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleException(ex);
        }
    }

    public ApparelModel createApparel(ApparelModel newApparel) {
        try {
            return restTemplate.postForObject(BASE_URL, newApparel, ApparelModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleException(ex);
        }
    }

    public ApparelModel updateApparel(String apparelId, ApparelModel updatedApparel) {
        try {
            String url = BASE_URL + "/" + apparelId;
            restTemplate.put(url, updatedApparel);
            return getApparelByApparelId(apparelId);
        } catch (HttpClientErrorException ex) {
            throw handleException(ex);
        }
    }

    public void deleteApparel(String apparelId) {
        try {
            String url = BASE_URL + "/" + apparelId;
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleException(ex);
        }
    }


    public int getStock(String apparelId) {
        try {
            String url = BASE_URL + "/" + apparelId + "/stock";
            return restTemplate.getForObject(url, Integer.class);
        } catch (HttpClientErrorException ex) {
            throw handleException(ex);
        }
    }

    public void decreaseStock(String apparelId, int quantity) {
        try {
            String url = BASE_URL + "/" + apparelId + "/stock/decrease?quantity=" + quantity;
            restTemplate.patchForObject(url, null, Void.class);
        } catch (HttpClientErrorException ex) {
            throw handleException(ex);
        }
    }

    public void increaseStock(String apparelId, int quantity) {
        try {
            String url = BASE_URL + "/" + apparelId + "/stock/increase?quantity=" + quantity;
            restTemplate.patchForObject(url, null, Void.class);
        } catch (HttpClientErrorException ex) {
            throw handleException(ex);
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

    private RuntimeException handleException(HttpClientErrorException ex) {
        String msg = extractErrorMessage(ex);

        if (ex.getStatusCode() == NOT_FOUND) {
            return new NotFoundException(msg);
        }
        if (ex.getStatusCode() == UNPROCESSABLE_ENTITY) {
            return new InvalidInputException(msg);
        }
        log.warn("Unexpected HTTP error from Apparels Service: {}", ex.getStatusCode());
        return ex;
    }
}
