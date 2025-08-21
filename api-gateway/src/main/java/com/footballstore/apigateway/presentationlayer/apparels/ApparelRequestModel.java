package com.footballstore.apigateway.presentationlayer.apparels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApparelRequestModel {
    private String itemName;
    private String description;
    private String brand;
    private BigDecimal price;
    private BigDecimal cost;
    private Integer stock;
    private String apparelType;
    private String sizeOption;
}
