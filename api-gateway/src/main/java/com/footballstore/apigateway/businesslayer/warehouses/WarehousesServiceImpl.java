package com.footballstore.apigateway.businesslayer.warehouses;

import com.footballstore.apigateway.domainclientlayer.warehouses.WarehousesServiceClient;
import com.footballstore.apigateway.presentationlayer.warehouses.WarehouseRequestModel;
import com.footballstore.apigateway.presentationlayer.warehouses.WarehouseResponseModel;
import com.footballstore.apigateway.utils.exceptions.InvalidInputException;
import com.footballstore.apigateway.utils.exceptions.InvalidWarehouseCapacityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Slf4j
@Service
public class WarehousesServiceImpl implements WarehousesService {

    private final WarehousesServiceClient warehousesServiceClient;

    public WarehousesServiceImpl(WarehousesServiceClient warehousesServiceClient) {
        this.warehousesServiceClient = warehousesServiceClient;
    }

    @Override
    public List<WarehouseResponseModel> getAllWarehouses() {
        log.debug("API-Gateway: Fetching all warehouses");
        List<WarehouseResponseModel> warehouses = warehousesServiceClient.getAllWarehouses();
        warehouses.forEach(this::enrichWithLinks);
        return warehouses;
    }

    @Override
    public WarehouseResponseModel getWarehouseById(String warehouseId) {
        log.debug("API-Gateway: Fetching warehouse {}", warehouseId);
        validateUuid(warehouseId);
        WarehouseResponseModel warehouse = warehousesServiceClient.getWarehouseById(warehouseId);
        enrichWithLinks(warehouse);
        return warehouse;
    }

    @Override
    public WarehouseResponseModel createWarehouse(WarehouseRequestModel requestModel) {
        log.debug("API-Gateway: Creating warehouse");
        if (requestModel.getCapacity() == null || requestModel.getCapacity() <= 0) {
            throw new InvalidWarehouseCapacityException("Warehouse capacity must be greater than zero");
        }
        WarehouseResponseModel created = warehousesServiceClient.createWarehouse(requestModel);
        enrichWithLinks(created);
        return created;
    }

    @Override
    public WarehouseResponseModel updateWarehouse(String warehouseId, WarehouseRequestModel requestModel) {
        log.debug("API-Gateway: Updating warehouse {}", warehouseId);
        validateUuid(warehouseId);
        WarehouseResponseModel updated = warehousesServiceClient.updateWarehouse(warehouseId, requestModel);
        enrichWithLinks(updated);
        return updated;
    }

    @Override
    public void deleteWarehouse(String warehouseId) {
        log.debug("API-Gateway: Deleting warehouse {}", warehouseId);
        validateUuid(warehouseId);
        warehousesServiceClient.deleteWarehouse(warehouseId);
    }

    private void enrichWithLinks(WarehouseResponseModel w) {
        if (w != null && w.getWarehouseId() != null) {
            w.add(
                    linkTo(methodOn(com.footballstore.apigateway.presentationlayer.warehouses.WarehousesController.class)
                            .getWarehouseById(w.getWarehouseId()))
                            .withSelfRel());
            w.add(
                    linkTo(methodOn(com.footballstore.apigateway.presentationlayer.warehouses.WarehousesController.class)
                            .getAllWarehouses())
                            .withRel("allWarehouses"));
        }
    }

    private void validateUuid(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Provided warehouseId is invalid: " + id);
        }
    }
}
