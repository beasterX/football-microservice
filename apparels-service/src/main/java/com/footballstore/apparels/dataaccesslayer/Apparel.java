package com.footballstore.apparels.dataaccesslayer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "APPARELS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Apparel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private ApparelIdentifier apparelIdentifier;

    @Column(name = "ITEM_NAME", nullable = false)
    private String itemName;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "BRAND")
    private String brand;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "COST")
    private BigDecimal cost;

    @Column(name = "STOCK")
    private Integer stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "APPAREL_TYPE")
    private ApparelType apparelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "SIZE_OPTION")
    private SizeOption sizeOption;
}
