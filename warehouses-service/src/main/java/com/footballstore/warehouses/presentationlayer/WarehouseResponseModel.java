package com.footballstore.warehouses.presentationlayer;

import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

@Data
public class WarehouseResponseModel extends RepresentationModel<WarehouseResponseModel> {
    private String warehouseId;
    private String locationName;
    private String address;
    private Integer capacity;
}
