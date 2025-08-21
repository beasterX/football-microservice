package com.footballstore.apigateway.presentationlayer.warehouses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseResponseModel extends RepresentationModel<WarehouseResponseModel> {
    private String warehouseId;
    private String locationName;
    private String address;
    private Integer capacity;
}
