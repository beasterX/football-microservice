package com.footballstore.warehouses.dataaccesslayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    Optional<Warehouse> findByWarehouseIdentifier_WarehouseId(String warehouseId);
}
