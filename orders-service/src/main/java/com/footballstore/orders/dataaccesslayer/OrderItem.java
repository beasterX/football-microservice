package com.footballstore.orders.dataaccesslayer;

import com.footballstore.orders.domainclientlayer.apparels.ApparelModel;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private OrderItemIdentifier orderItemIdentifier;

    private ApparelModel apparelModel;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal discount;

    private BigDecimal lineTotal;
}
