package com.footballstore.warehouses.businesslayer;

import com.footballstore.warehouses.presentationlayer.WarehouseRequestModel;
import com.footballstore.warehouses.presentationlayer.WarehouseResponseModel;

import java.util.List;

public interface WarehouseService {
    List<WarehouseResponseModel> getAllWarehouses();
    WarehouseResponseModel getWarehouseById(String warehouseId);
    WarehouseResponseModel createWarehouse(WarehouseRequestModel requestModel);
    WarehouseResponseModel updateWarehouse(String warehouseId, WarehouseRequestModel requestModel);
    void deleteWarehouse(String warehouseId);
}
