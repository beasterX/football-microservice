package com.footballstore.orders.domainclientlayer.warehouses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseModel {
    private String warehouseId;
    private String locationName;
    private String address;
    private Integer capacity;
}
