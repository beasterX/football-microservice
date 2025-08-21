package com.footballstore.warehouses.datamappinglayer;

import java.util.List;

import com.footballstore.warehouses.dataaccesslayer.Warehouse;
import com.footballstore.warehouses.presentationlayer.WarehouseController;
import com.footballstore.warehouses.presentationlayer.WarehouseResponseModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Mapper(componentModel = "spring")
public interface WarehouseResponseMapper {

    @Mapping(source = "warehouseIdentifier.warehouseId", target = "warehouseId")
    WarehouseResponseModel entityToResponseModel(Warehouse warehouse);

    List<WarehouseResponseModel> entityListToResponseModelList(List<Warehouse> warehouses);

}
