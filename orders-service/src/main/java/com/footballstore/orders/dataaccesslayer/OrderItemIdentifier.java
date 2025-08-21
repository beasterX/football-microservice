package com.footballstore.orders.dataaccesslayer;

import lombok.Getter;
import org.springframework.data.mongodb.core.index.Indexed;
import java.util.UUID;


@Getter
public class OrderItemIdentifier {

    @Indexed
    private final String orderItemId = UUID.randomUUID().toString();
}
