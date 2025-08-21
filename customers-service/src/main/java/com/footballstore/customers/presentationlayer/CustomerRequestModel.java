package com.footballstore.customers.presentationlayer;

import com.footballstore.customers.dataaccesslayer.ContactMethod;
import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class CustomerRequestModel {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private ContactMethod preferredContact;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}