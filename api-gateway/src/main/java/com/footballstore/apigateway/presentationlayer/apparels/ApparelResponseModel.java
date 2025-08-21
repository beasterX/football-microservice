package com.footballstore.apigateway.presentationlayer.apparels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApparelResponseModel extends RepresentationModel<ApparelResponseModel> {
    private String apparelId;
    private String itemName;
    private String description;
    private String brand;
    private BigDecimal price;
    private BigDecimal cost;
    private Integer stock;
    private String apparelType;
    private String sizeOption;
}
