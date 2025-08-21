package com.footballstore.warehouses.dataaccesslayer;

import jakarta.persistence.*;
import lombok.*;
import java.util.Objects;

@Entity
@Table(name = "WAREHOUSES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private WarehouseIdentifier warehouseIdentifier;

    @Column(name = "LOCATION_NAME")
    private String locationName;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "CAPACITY")
    private Integer capacity;

}
