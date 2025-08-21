package com.footballstore.apparels.presentationlayer;

import com.footballstore.apparels.dataaccesslayer.ApparelType;
import com.footballstore.apparels.dataaccesslayer.SizeOption;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;

@Data
public class ApparelResponseModel extends RepresentationModel<ApparelResponseModel> {
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
