package com.footballstore.orders.mappinglayer;

import com.footballstore.orders.dataaccesslayer.*;
import com.footballstore.orders.domainclientlayer.customers.CustomerModel;
import com.footballstore.orders.domainclientlayer.warehouses.WarehouseModel;
import com.footballstore.orders.presentationlayer.OrderRequestModel;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderIdentifier", source = "orderIdentifier")
    @Mapping(target = "customerModel", source = "customer")
    @Mapping(target = "warehouseModel", source = "warehouse")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "orderDate", source = "orderDate")
    Order mapToOrderEntity(
            OrderIdentifier orderIdentifier,
            CustomerModel customer,
            WarehouseModel warehouse,
            OrderRequestModel request,
            List<OrderItem> items,
            OrderPrice totalPrice,
            LocalDate orderDate
    );
}
