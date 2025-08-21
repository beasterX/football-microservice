package com.footballstore.warehouses.presentationlayer;

import com.footballstore.warehouses.businesslayer.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public ResponseEntity<List<WarehouseResponseModel>> getAllWarehouses() {
        List<WarehouseResponseModel> warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(warehouses);
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponseModel> getWarehouseById(@PathVariable String warehouseId) {
        WarehouseResponseModel warehouse = warehouseService.getWarehouseById(warehouseId);
        return ResponseEntity.ok(warehouse);
    }

    @PostMapping
    public ResponseEntity<WarehouseResponseModel> createWarehouse(@RequestBody WarehouseRequestModel requestModel) {
        WarehouseResponseModel created = warehouseService.createWarehouse(requestModel);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponseModel> updateWarehouse(@PathVariable String warehouseId,
                                                                  @RequestBody WarehouseRequestModel requestModel) {
        WarehouseResponseModel updated = warehouseService.updateWarehouse(warehouseId, requestModel);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable String warehouseId) {
        warehouseService.deleteWarehouse(warehouseId);
        return ResponseEntity.noContent().build();
    }
}
