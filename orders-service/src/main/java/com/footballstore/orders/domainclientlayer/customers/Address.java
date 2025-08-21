package com.footballstore.orders.domainclientlayer.customers;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
