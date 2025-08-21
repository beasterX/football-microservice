package com.footballstore.orders.presentationlayer;

import com.footballstore.orders.domainclientlayer.apparels.ApparelType;
import com.footballstore.orders.domainclientlayer.apparels.SizeOption;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponseModel {

    private String orderItemId;

    private String apparelId;

    private String itemName;

    private String description;

    private String brand;

    private BigDecimal unitPrice;

    private BigDecimal cost;

    private Integer quantity;

    private BigDecimal discount;

    private BigDecimal lineTotal;

    private ApparelType apparelType;

    private SizeOption sizeOption;
}
