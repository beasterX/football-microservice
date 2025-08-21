package com.footballstore.warehouses.businesslayer;

import com.footballstore.warehouses.dataaccesslayer.Warehouse;
import com.footballstore.warehouses.dataaccesslayer.WarehouseIdentifier;
import com.footballstore.warehouses.dataaccesslayer.WarehouseRepository;
import com.footballstore.warehouses.datamappinglayer.WarehouseRequestMapper;
import com.footballstore.warehouses.datamappinglayer.WarehouseResponseMapper;
import com.footballstore.warehouses.presentationlayer.WarehouseRequestModel;
import com.footballstore.warehouses.presentationlayer.WarehouseResponseModel;
import com.footballstore.warehouses.utils.exceptions.InvalidWarehouseCapacityException;
import com.footballstore.warehouses.utils.exceptions.InvalidInputException;
import com.footballstore.warehouses.utils.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseResponseMapper warehouseResponseMapper;
    private final WarehouseRequestMapper warehouseRequestMapper;

    @Override
    public List<WarehouseResponseModel> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        return warehouseResponseMapper.entityListToResponseModelList(warehouses);
    }

    @Override
    public WarehouseResponseModel getWarehouseById(String warehouseId) {
        validateUuid(warehouseId);
        Warehouse warehouse = warehouseRepository.findByWarehouseIdentifier_WarehouseId(warehouseId)
                .orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + warehouseId));
        return warehouseResponseMapper.entityToResponseModel(warehouse);
    }

    @Override
    public WarehouseResponseModel createWarehouse(WarehouseRequestModel requestModel) {
        if (requestModel.getCapacity() == null || requestModel.getCapacity() < 100) {
            throw new InvalidWarehouseCapacityException("Warehouse capacity must be at least 100.");
        }
        Warehouse warehouse = warehouseRequestMapper.requestModelToEntity(requestModel);
        warehouse.setWarehouseIdentifier(WarehouseIdentifier.generate());
        Warehouse saved = warehouseRepository.save(warehouse);
        return warehouseResponseMapper.entityToResponseModel(saved);
    }

    @Override
    public WarehouseResponseModel updateWarehouse(String warehouseId, WarehouseRequestModel requestModel) {
        validateUuid(warehouseId);
        Warehouse existing = warehouseRepository.findByWarehouseIdentifier_WarehouseId(warehouseId)
                .orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + warehouseId));
        if (requestModel.getCapacity() != null && requestModel.getCapacity() < 100) {
            throw new InvalidWarehouseCapacityException("Warehouse capacity must be at least 100.");
        }
        existing.setLocationName(requestModel.getLocationName());
        existing.setAddress(requestModel.getAddress());
        existing.setCapacity(requestModel.getCapacity());
        Warehouse saved = warehouseRepository.save(existing);
        return warehouseResponseMapper.entityToResponseModel(saved);
    }

    @Override
    public void deleteWarehouse(String warehouseId) {
        validateUuid(warehouseId);
        Warehouse existing = warehouseRepository.findByWarehouseIdentifier_WarehouseId(warehouseId)
                .orElseThrow(() -> new NotFoundException("Warehouse not found with id: " + warehouseId));
        warehouseRepository.delete(existing);
    }

    private void validateUuid(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new InvalidInputException("Provided warehouseId is invalid: " + id);
        }
    }
}
