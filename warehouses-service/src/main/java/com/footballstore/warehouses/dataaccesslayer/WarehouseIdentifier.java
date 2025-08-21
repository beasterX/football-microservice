package com.footballstore.warehouses.dataaccesslayer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import java.util.UUID;

@Embeddable
@Data
public class WarehouseIdentifier {

    @Column(name = "WAREHOUSE_ID")
    private String warehouseId;

    protected WarehouseIdentifier() {
    }

    public WarehouseIdentifier(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public static WarehouseIdentifier generate() {
        return new WarehouseIdentifier(UUID.randomUUID().toString());
    }
}
