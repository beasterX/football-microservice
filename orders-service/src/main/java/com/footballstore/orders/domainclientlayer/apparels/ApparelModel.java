package com.footballstore.orders.domainclientlayer.apparels;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApparelModel {
    private String apparelId;
    private String itemName;
    private String description;
    private String brand;
    private BigDecimal price;
    private BigDecimal cost;
    private Integer stock;
    private ApparelType apparelType;
    private SizeOption sizeOption;
}
