package com.footballstore.orders.mappinglayer;

import com.footballstore.orders.dataaccesslayer.Order;
import com.footballstore.orders.dataaccesslayer.OrderItem;
import com.footballstore.orders.presentationlayer.OrderItemResponseModel;
import com.footballstore.orders.presentationlayer.OrderResponseModel;
import org.mapstruct.*;
import org.springframework.hateoas.Link;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Mapper(componentModel = "spring")
public interface OrderResponseMapper {

    @Mapping(target = "orderId",           expression = "java(order.getOrderIdentifier().getOrderId())")
    @Mapping(target = "orderDate",         source = "orderDate")

    // customer
    @Mapping(target = "customerId",        expression = "java(order.getCustomerModel().getCustomerId())")
    @Mapping(target = "firstName",         expression = "java(order.getCustomerModel().getFirstName())")
    @Mapping(target = "lastName",          expression = "java(order.getCustomerModel().getLastName())")
    @Mapping(target = "email",             expression = "java(order.getCustomerModel().getEmail())")
    @Mapping(target = "phone",             expression = "java(order.getCustomerModel().getPhone())")
    @Mapping(target = "registrationDate",  expression = "java(order.getCustomerModel().getRegistrationDate())")
    @Mapping(target = "preferredContact",  expression = "java(order.getCustomerModel().getPreferredContact())")
    @Mapping(target = "address",           expression = "java(order.getCustomerModel().getAddress())")

    // warehouse
    @Mapping(target = "warehouseId",       expression = "java(order.getWarehouseModel().getWarehouseId())")
    @Mapping(target = "locationName",      expression = "java(order.getWarehouseModel().getLocationName())")
    @Mapping(target = "warehouseAddress",  expression = "java(order.getWarehouseModel().getAddress())")
    @Mapping(target = "capacity",          expression = "java(order.getWarehouseModel().getCapacity())")

    @Mapping(target = "items",             source = "items")
    @Mapping(target = "totalAmount",       expression = "java(order.getTotalPrice().getAmount())")
    @Mapping(target = "currency",          expression = "java(order.getTotalPrice().getCurrency())")
    @Mapping(target = "orderStatus",       source = "orderStatus")
    @Mapping(target = "paymentStatus",     source = "paymentStatus")
    OrderResponseModel mapToOrderResponse(Order order);

    @Mapping(target = "orderItemId",        expression = "java(item.getOrderItemIdentifier().getOrderItemId())")
    @Mapping(target = "apparelId",          expression = "java(item.getApparelModel().getApparelId())")
    @Mapping(target = "itemName",           expression = "java(item.getApparelModel().getItemName())")
    @Mapping(target = "description",        expression = "java(item.getApparelModel().getDescription())")
    @Mapping(target = "brand",              expression = "java(item.getApparelModel().getBrand())")
    @Mapping(target = "unitPrice",          source = "unitPrice")
    @Mapping(target = "cost",               expression = "java(item.getApparelModel().getCost())")
    @Mapping(target = "quantity",           source = "quantity")
    @Mapping(target = "discount",           source = "discount")
    @Mapping(target = "lineTotal",          source = "lineTotal")
    @Mapping(target = "apparelType",        expression = "java(item.getApparelModel().getApparelType())")
    @Mapping(target = "sizeOption",         expression = "java(item.getApparelModel().getSizeOption())")
    OrderItemResponseModel mapToOrderItemResponse(OrderItem item);

//    @AfterMapping
//    default void addHateoasLinks(@MappingTarget OrderResponseModel model, Order order) {
//        Link self = linkTo(methodOn(
//                com.footballstore.orders.presentationlayer.OrderController.class)
//                .getCustomerOrderById(
//                        order.getCustomerModel().getCustomerId(),
//                        order.getOrderIdentifier().getOrderId()))
//                .withSelfRel();
//        model.add(self);
//
//        Link all = linkTo(methodOn(
//                com.footballstore.orders.presentationlayer.OrderController.class)
//                .getAllCustomerOrders(
//                        order.getCustomerModel().getCustomerId()))
//                .withRel("all-orders");
//        model.add(all);
//    }
}
