package com.footballstore.apparels.presentationlayer;

import com.footballstore.apparels.dataaccesslayer.ApparelType;
import com.footballstore.apparels.dataaccesslayer.SizeOption;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Builder
@Data
public class ApparelRequestModel {
    private String itemName;
    private String description;
    private String brand;
    private BigDecimal price;
    private BigDecimal cost;
    private Integer stock;
    private ApparelType apparelType;
    private SizeOption sizeOption;
}
