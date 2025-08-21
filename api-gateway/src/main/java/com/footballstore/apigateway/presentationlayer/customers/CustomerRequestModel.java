package com.footballstore.apigateway.presentationlayer.customers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRequestModel {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String preferredContact;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
