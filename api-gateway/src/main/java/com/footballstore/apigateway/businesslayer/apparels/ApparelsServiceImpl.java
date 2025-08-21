package com.footballstore.apigateway.businesslayer.apparels;

import com.footballstore.apigateway.domainclientlayer.apparels.ApparelsServiceClient;
import com.footballstore.apigateway.presentationlayer.apparels.ApparelRequestModel;
import com.footballstore.apigateway.presentationlayer.apparels.ApparelResponseModel;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@Service
public class ApparelsServiceImpl implements ApparelsService {

    private final ApparelsServiceClient apparelsServiceClient;

    public ApparelsServiceImpl(ApparelsServiceClient apparelsServiceClient) {
        this.apparelsServiceClient = apparelsServiceClient;
    }

    @Override
    public List<ApparelResponseModel> getAllApparels() {
        List<ApparelResponseModel> apparels = apparelsServiceClient.getAllApparels();
        for (ApparelResponseModel a : apparels) {
            enrichWithLinks(a);
        }
        return apparels;
    }

    @Override
    public ApparelResponseModel getApparelById(String apparelId) {
        validateUuid(apparelId);
        ApparelResponseModel apparel = apparelsServiceClient.getApparelById(apparelId);
        enrichWithLinks(apparel);
        return apparel;
    }

    @Override
    public ApparelResponseModel createApparel(ApparelRequestModel requestModel) {
        ApparelResponseModel created = apparelsServiceClient.createApparel(requestModel);
        enrichWithLinks(created);
        return created;
    }

    @Override
    public ApparelResponseModel updateApparel(String apparelId, ApparelRequestModel requestModel) {
        validateUuid(apparelId);
        ApparelResponseModel updated = apparelsServiceClient.updateApparel(apparelId, requestModel);
        enrichWithLinks(updated);
        return updated;
    }

    @Override
    public ApparelResponseModel deleteApparel(String apparelId) {
        validateUuid(apparelId);
        apparelsServiceClient.deleteApparel(apparelId);
        return null;
    }

    private void enrichWithLinks(ApparelResponseModel apparel) {
        if (apparel != null && apparel.getApparelId() != null) {
            apparel.add(linkTo(
                    methodOn(com.footballstore.apigateway.presentationlayer.apparels.ApparelsController.class)
                            .getApparelById(apparel.getApparelId()))
                    .withSelfRel());
            apparel.add(linkTo(
                    methodOn(com.footballstore.apigateway.presentationlayer.apparels.ApparelsController.class)
                            .getAllApparels())
                    .withRel("allApparels"));
        }
    }

    private void validateUuid(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Provided apparelId is invalid: " + id);
        }
    }
}
