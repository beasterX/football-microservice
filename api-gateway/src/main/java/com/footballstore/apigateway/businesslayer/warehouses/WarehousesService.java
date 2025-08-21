package com.footballstore.apigateway.businesslayer.warehouses;

import com.footballstore.apigateway.presentationlayer.warehouses.WarehouseRequestModel;
import com.footballstore.apigateway.presentationlayer.warehouses.WarehouseResponseModel;

import java.util.List;

public interface WarehousesService {
    List<WarehouseResponseModel> getAllWarehouses();

    WarehouseResponseModel getWarehouseById(String warehouseId);

    WarehouseResponseModel createWarehouse(WarehouseRequestModel requestModel);

    WarehouseResponseModel updateWarehouse(String warehouseId, WarehouseRequestModel requestModel);

    void deleteWarehouse(String warehouseId);
}
