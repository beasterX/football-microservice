package com.footballstore.apigateway.presentationlayer.warehouses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseRequestModel {
    private String locationName;
    private String address;
    private Integer capacity;
}
