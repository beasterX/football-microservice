package com.footballstore.warehouses.datamappinglayer;

import com.footballstore.warehouses.dataaccesslayer.Warehouse;
import com.footballstore.warehouses.presentationlayer.WarehouseRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface WarehouseRequestMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "warehouseIdentifier", ignore = true)
    })
    Warehouse requestModelToEntity(WarehouseRequestModel requestModel);
}
