package com.footballstore.apparels.businesslayer;

import com.footballstore.apparels.dataaccesslayer.Apparel;
import com.footballstore.apparels.dataaccesslayer.ApparelIdentifier;
import com.footballstore.apparels.dataaccesslayer.ApparelRepository;
import com.footballstore.apparels.datamapperlayer.ApparelRequestMapper;
import com.footballstore.apparels.datamapperlayer.ApparelResponseMapper;
import com.footballstore.apparels.presentationlayer.ApparelRequestModel;
import com.footballstore.apparels.presentationlayer.ApparelResponseModel;
import com.footballstore.apparels.utils.exceptions.InvalidApparelPricingException;
import com.footballstore.apparels.utils.exceptions.InvalidInputException;
import com.footballstore.apparels.utils.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApparelServiceImpl implements ApparelService {

    private final ApparelRepository apparelRepository;
    private final ApparelResponseMapper apparelResponseMapper;
    private final ApparelRequestMapper apparelRequestMapper;

    @Override
    public List<ApparelResponseModel> getAllApparels() {
        return apparelResponseMapper.entityListToResponseModelList(apparelRepository.findAll());
    }

    @Override
    public ApparelResponseModel getApparelById(String apparelId) {
        validateUuid(apparelId);
        Apparel a = apparelRepository.findByApparelIdentifier_ApparelId(apparelId)
                .orElseThrow(() -> new NotFoundException("Apparel not found with id: " + apparelId));
        return apparelResponseMapper.entityToResponseModel(a);
    }

    @Override
    public ApparelResponseModel createApparel(ApparelRequestModel requestModel) {
        if (requestModel.getCost() != null && requestModel.getPrice() != null &&
                requestModel.getCost().compareTo(requestModel.getPrice()) > 0) {
                throw new InvalidApparelPricingException(
                    "Cost cannot exceed price for apparel: " + requestModel.getItemName());
        }
        Apparel e = apparelRequestMapper.requestModelToEntity(requestModel);
        e.setApparelIdentifier(ApparelIdentifier.generate());
        return apparelResponseMapper.entityToResponseModel(apparelRepository.save(e));
    }

    @Override
    public ApparelResponseModel updateApparel(String apparelId, ApparelRequestModel requestModel) {
        validateUuid(apparelId);

        if (requestModel.getCost() != null
                && requestModel.getPrice() != null
                && requestModel.getCost().compareTo(requestModel.getPrice()) > 0) {
            throw new InvalidApparelPricingException(
                    "Cost cannot exceed price for apparel: " + requestModel.getItemName());
        }

        Apparel existing = apparelRepository
                .findByApparelIdentifier_ApparelId(apparelId)
                .orElseThrow(() ->
                        new NotFoundException("Apparel not found with id: " + apparelId)
                );

        existing.setItemName(requestModel.getItemName());
        existing.setDescription(requestModel.getDescription());
        existing.setBrand(requestModel.getBrand());
        existing.setPrice(requestModel.getPrice());
        existing.setCost(requestModel.getCost());
        existing.setStock(requestModel.getStock());
        existing.setApparelType(requestModel.getApparelType());
        existing.setSizeOption(requestModel.getSizeOption());

        Apparel saved = apparelRepository.save(existing);
        return apparelResponseMapper.entityToResponseModel(saved);
    }


    @Override
    public void deleteApparel(String apparelId) {
        validateUuid(apparelId);
        Apparel existing = apparelRepository.findByApparelIdentifier_ApparelId(apparelId)
                .orElseThrow(() -> new NotFoundException("Apparel not found with id: " + apparelId));
        apparelRepository.delete(existing);
    }


    @Override
    public int getStock(String apparelId) {
        validateUuid(apparelId);
        Apparel existing = apparelRepository.findByApparelIdentifier_ApparelId(apparelId)
                .orElseThrow(() -> new NotFoundException("Apparel not found with id: " + apparelId));
        return existing.getStock();
    }

    @Override
    public void decreaseStock(String apparelId, int quantity) {
        validateUuid(apparelId);
        Apparel existing = apparelRepository.findByApparelIdentifier_ApparelId(apparelId)
                .orElseThrow(() -> new NotFoundException("Apparel not found with id: " + apparelId));
        if (existing.getStock() < quantity) {
            throw new InvalidInputException("Not enough stock for apparel: " + apparelId);
        }
        existing.setStock(existing.getStock() - quantity);
        apparelRepository.save(existing);
    }

    @Override
    public void increaseStock(String apparelId, int quantity) {
        validateUuid(apparelId);
        Apparel existing = apparelRepository.findByApparelIdentifier_ApparelId(apparelId)
                .orElseThrow(() -> new NotFoundException("Apparel not found with id: " + apparelId));
        existing.setStock(existing.getStock() + quantity);
        apparelRepository.save(existing);
    }

    private void validateUuid(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Provided apparelId is invalid: " + id);
        }
    }
}
