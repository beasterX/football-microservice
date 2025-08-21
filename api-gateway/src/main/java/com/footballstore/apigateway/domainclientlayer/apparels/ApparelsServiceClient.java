package com.footballstore.apigateway.domainclientlayer.apparels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballstore.apigateway.presentationlayer.apparels.ApparelRequestModel;
import com.footballstore.apigateway.presentationlayer.apparels.ApparelResponseModel;
import com.footballstore.apigateway.utils.HttpErrorInfo;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import com.footballstore.apigateway.utils.exceptions.NotFoundException;
import com.footballstore.apigateway.utils.exceptions.InvalidApparelPricingException;
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
    private final String APPARELS_SERVICE_BASE_URL;

    public ApparelsServiceClient(RestTemplate restTemplate, ObjectMapper mapper,
                                 @Value("${app.apparels-service.host}") String apparelsServiceHost,
                                 @Value("${app.apparels-service.port}") String apparelsServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
        this.APPARELS_SERVICE_BASE_URL = "http://" + apparelsServiceHost + ":" + apparelsServicePort + "/api/v1/apparels";
    }

    public List<ApparelResponseModel> getAllApparels() {
        log.debug("API-Gateway: Fetching all apparels");
        try {
            ResponseEntity<ApparelResponseModel[]> responseEntity =
                    restTemplate.getForEntity(APPARELS_SERVICE_BASE_URL, ApparelResponseModel[].class);
            ApparelResponseModel[] array = responseEntity.getBody();
            return array != null ? Arrays.asList(array) : new ArrayList<>();
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public ApparelResponseModel getApparelById(String apparelId) {
        log.debug("API-Gateway: Fetching apparel with id: {}", apparelId);
        try {
            String url = APPARELS_SERVICE_BASE_URL + "/" + apparelId;
            return restTemplate.getForObject(url, ApparelResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public ApparelResponseModel createApparel(ApparelRequestModel request) {
        log.debug("API-Gateway: Creating apparel");
        try {
            return restTemplate.postForObject(APPARELS_SERVICE_BASE_URL, request, ApparelResponseModel.class);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public ApparelResponseModel updateApparel(String apparelId, ApparelRequestModel request) {
        log.debug("API-Gateway: Updating apparel with id: {}", apparelId);
        try {
            String url = APPARELS_SERVICE_BASE_URL + "/" + apparelId;
            restTemplate.put(url, request);
            return getApparelById(apparelId);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    public void deleteApparel(String apparelId) {
        log.debug("API-Gateway: Deleting apparel with id: {}", apparelId);
        try {
            String url = APPARELS_SERVICE_BASE_URL + "/" + apparelId;
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
            if (message != null && message.toLowerCase().contains("pricing")) {
                return new InvalidApparelPricingException(message);
            }
            return new InvalidInputException(message);
        }
        log.warn("Unexpected HTTP error: {}", ex.getStatusCode());
        return ex;
    }
}
