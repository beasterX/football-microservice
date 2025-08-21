package com.footballstore.apigateway.presentationlayer.warehouses;

import com.footballstore.apigateway.businesslayer.warehouses.WarehousesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehousesController {

    private final WarehousesService service;

    public WarehousesController(WarehousesService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<List<WarehouseResponseModel>> getAllWarehouses() {
        log.debug("GET all warehouses");
        return ResponseEntity.ok(service.getAllWarehouses());
    }

    @GetMapping(value = "/{warehouseId}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<WarehouseResponseModel> getWarehouseById(
            @PathVariable String warehouseId) {

        log.debug("GET warehouse {}", warehouseId);
        return ResponseEntity.ok(service.getWarehouseById(warehouseId));
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<WarehouseResponseModel> createWarehouse(
            @RequestBody WarehouseRequestModel requestModel) {

        log.debug("POST new warehouse");
        var created = service.createWarehouse(requestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(
            value = "/{warehouseId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<WarehouseResponseModel> updateWarehouse(
            @PathVariable String warehouseId,
            @RequestBody WarehouseRequestModel requestModel) {

        log.debug("PUT warehouse {}", warehouseId);
        var updated = service.updateWarehouse(warehouseId, requestModel);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable String warehouseId) {
        log.debug("DELETE warehouse {}", warehouseId);
        service.deleteWarehouse(warehouseId);
        return ResponseEntity.noContent().build();
    }
}
