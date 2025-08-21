package com.footballstore.orders.utils;

import com.footballstore.orders.dataaccesslayer.*;
import com.footballstore.orders.domainclientlayer.apparels.ApparelModel;
import com.footballstore.orders.domainclientlayer.apparels.ApparelType;
import com.footballstore.orders.domainclientlayer.apparels.SizeOption;
import com.footballstore.orders.domainclientlayer.customers.Address;
import com.footballstore.orders.domainclientlayer.customers.ContactMethod;
import com.footballstore.orders.domainclientlayer.customers.CustomerModel;
import com.footballstore.orders.domainclientlayer.warehouses.WarehouseModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DatabaseLoaderService implements CommandLineRunner {

    private final OrderRepository orderRepository;

    public DatabaseLoaderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {

        List<Order> orders = new ArrayList<>();

        String[] manualIds = new String[]{
                "00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-000000000002",
                "00000000-0000-0000-0000-000000000003",
                "00000000-0000-0000-0000-000000000004",
                "00000000-0000-0000-0000-000000000005",
                "00000000-0000-0000-0000-000000000006",
                "00000000-0000-0000-0000-000000000007",
                "00000000-0000-0000-0000-000000000008",
                "00000000-0000-0000-0000-000000000009",
                "00000000-0000-0000-0000-00000000000A",
                "00000000-0000-0000-0000-00000000000B",
                "00000000-0000-0000-0000-00000000000C",
                "00000000-0000-0000-0000-00000000000D",
                "00000000-0000-0000-0000-00000000000E",
                "00000000-0000-0000-0000-00000000000F"
        };

        // Messi @ Central Warehouse, Adidas Home Jersey
        CustomerModel messi = CustomerModel.builder()
                .customerId("3fa85f64-5717-4562-b3fc-2c963f66afa6")
                .firstName("Lionel").lastName("Messi")
                .email("lionel.messi@gmail.com").phone("1112223333")
                .registrationDate(LocalDate.of(2023,7,15))
                .preferredContact(ContactMethod.EMAIL)
                .address(new Address("Avenida del Libertador 1234","Rosario","Santa Fe","Argentina","2000"))
                .build();
        WarehouseModel centralWh = WarehouseModel.builder()
                .warehouseId("11111111-2222-3333-4444-555555555555")
                .locationName("Central Warehouse")
                .address("123 Warehouse Ave, Springfield")
                .capacity(500)
                .build();
        ApparelModel homeJersey = ApparelModel.builder()
                .apparelId("aaa11111-bbbb-cccc-dddd-eeeeeeeeeeee")
                .itemName("Adidas Home Jersey")
                .description("Home jersey for season 2023")
                .brand("Adidas")
                .price(new BigDecimal("59.99"))
                .cost(new BigDecimal("30.00"))
                .stock(120)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.M)
                .build();
        for (int i = 0; i < 5; i++) {
            orders.add(buildOrder(
                    manualIds[i],
                    messi, centralWh, homeJersey,
                    /*qty*/ 1,
                    LocalDate.of(2025,5,1).plusDays(i)
            ));
        }

        // Ronaldo @ North Distribution, Adidas Away Jersey
        CustomerModel ronaldo = CustomerModel.builder()
                .customerId("7e2a4b80-8f09-4d53-9c0a-123456789abc")
                .firstName("Cristiano").lastName("Ronaldo")
                .email("cristiano.ronaldo@yahoo.com").phone("4445556666")
                .registrationDate(LocalDate.of(2022,11,20))
                .preferredContact(ContactMethod.PHONE)
                .address(new Address("Avenida dos Estados 5678","Funchal","Madeira","Portugal","9000"))
                .build();
        WarehouseModel northWh = WarehouseModel.builder()
                .warehouseId("66666666-7777-8888-9999-aaaaaaaaaaaa")
                .locationName("North Distribution Center")
                .address("456 North St, Metropolis")
                .capacity(300)
                .build();
        ApparelModel awayJersey = ApparelModel.builder()
                .apparelId("aaa22222-bbbb-cccc-dddd-eeeeeeeeeeee")
                .itemName("Adidas Away Jersey")
                .description("Away jersey for season 2023")
                .brand("Adidas")
                .price(new BigDecimal("61.50"))
                .cost(new BigDecimal("32.00"))
                .stock(100)
                .apparelType(ApparelType.JERSEY)
                .sizeOption(SizeOption.L)
                .build();
        for (int i = 0; i < 5; i++) {
            orders.add(buildOrder(
                    manualIds[5 + i],
                    ronaldo, northWh, awayJersey,
                    /*qty*/ 1,
                    LocalDate.of(2025,5,6).plusDays(i)
            ));
        }

        // Neymar @ East Storage, Nike Training Shorts
        CustomerModel neymar = CustomerModel.builder()
                .customerId("11111111-2222-3333-4444-555555555555")
                .firstName("Neymar").lastName("Jr")
                .email("neymar.jr@gmail.com").phone("7778889999")
                .registrationDate(LocalDate.of(2021,5,10))
                .preferredContact(ContactMethod.EMAIL)
                .address(new Address("Rua das Palmeiras 101","Santos","Sao Paulo","Brazil","1100"))
                .build();
        WarehouseModel eastWh = WarehouseModel.builder()
                .warehouseId("bbbbbbbb-cccc-dddd-eeee-ffffffffffff")
                .locationName("East Storage")
                .address("789 East Rd, Gotham")
                .capacity(200)
                .build();
        ApparelModel shorts = ApparelModel.builder()
                .apparelId("aaa33333-bbbb-cccc-dddd-eeeeeeeeeeee")
                .itemName("Nike Training Shorts")
                .description("Comfortable training shorts")
                .brand("Nike")
                .price(new BigDecimal("39.99"))
                .cost(new BigDecimal("20.00"))
                .stock(150)
                .apparelType(ApparelType.SHORTS)
                .sizeOption(SizeOption.S)
                .build();
        for (int i = 0; i < 5; i++) {
            orders.add(buildOrder(
                    manualIds[10 + i],
                    neymar, eastWh, shorts,
                    /*qty*/ 1,
                    LocalDate.of(2025,5,11).plusDays(i)
            ));
        }

        orderRepository.saveAll(orders);
    }

    private Order buildOrder(
            String manualOrderId,
            CustomerModel cust,
            WarehouseModel wh,
            ApparelModel app,
            int quantity,
            LocalDate date
    ) {
        var itemId = new OrderItemIdentifier();
        var lineTotal = app.getPrice().multiply(BigDecimal.valueOf(quantity));

        var orderItem = OrderItem.builder()
                .orderItemIdentifier(itemId)
                .apparelModel(app)
                .quantity(quantity)
                .unitPrice(app.getPrice())
                .discount(BigDecimal.ZERO)
                .lineTotal(lineTotal)
                .build();

        return Order.builder()
                .orderIdentifier(new OrderIdentifier(manualOrderId))
                .customerModel(cust)
                .warehouseModel(wh)
                .items(List.of(orderItem))
                .totalPrice(new OrderPrice(lineTotal, "USD"))
                .orderStatus(OrderStatus.CREATED)
                .paymentStatus(PaymentStatus.PENDING)
                .orderDate(date)
                .build();
    }
}
