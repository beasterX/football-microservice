package com.footballstore.warehouses.dataaccesslayer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class WarehouseRepositoryTest {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @BeforeEach
    public void setUp() {
        warehouseRepository.deleteAll();
    }

    @Test
    public void testSaveWarehouse_Positive_thenWarehouseIsPersisted() {
        Warehouse warehouse = Warehouse.builder()
                .warehouseIdentifier(new WarehouseIdentifier("WH001"))
                .locationName("Central Warehouse")
                .address("123 Warehouse Ave")
                .capacity(500)
                .build();

        Warehouse savedWarehouse = warehouseRepository.save(warehouse);

        assertNotNull(savedWarehouse);
        assertNotNull(savedWarehouse.getId(), "Saved warehouse should have a generated id");
        assertEquals("WH001", savedWarehouse.getWarehouseIdentifier().getWarehouseId());
        assertEquals("Central Warehouse", savedWarehouse.getLocationName());
        assertEquals(500, savedWarehouse.getCapacity());
    }

    @Test
    public void testFindByWarehouseId_Positive_thenReturnWarehouse() {
        Warehouse warehouse = Warehouse.builder()
                .warehouseIdentifier(new WarehouseIdentifier("WH002"))
                .locationName("North Depot")
                .address("456 North St")
                .capacity(300)
                .build();
        warehouseRepository.save(warehouse);

        Optional<Warehouse> found = warehouseRepository.findByWarehouseIdentifier_WarehouseId("WH002");
        assertTrue(found.isPresent());
        assertEquals("North Depot", found.get().getLocationName());
    }

    @Test
    public void testFindByNonexistentWarehouseId_Negative_thenReturnEmptyOptional() {
        Optional<Warehouse> found = warehouseRepository.findByWarehouseIdentifier_WarehouseId("NON_EXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    public void testDeleteWarehouse_Positive_thenRepositorySizeDecreases() {
        Warehouse warehouse = Warehouse.builder()
                .warehouseIdentifier(new WarehouseIdentifier("WH003"))
                .locationName("East Storage")
                .address("789 East Rd")
                .capacity(200)
                .build();
        Warehouse saved = warehouseRepository.save(warehouse);
        warehouseRepository.delete(saved);
        List<Warehouse> allWarehouses = warehouseRepository.findAll();
        assertEquals(0, allWarehouses.size());
    }
}
