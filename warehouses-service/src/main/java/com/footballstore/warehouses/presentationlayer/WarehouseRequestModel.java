package com.footballstore.warehouses.presentationlayer;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class WarehouseRequestModel {
    private String locationName;
    private String address;
    private Integer capacity;
}
