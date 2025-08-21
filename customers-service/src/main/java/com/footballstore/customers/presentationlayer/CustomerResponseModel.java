package com.footballstore.customers.presentationlayer;


import com.footballstore.customers.dataaccesslayer.ContactMethod;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;

@Data
public class CustomerResponseModel extends RepresentationModel<CustomerResponseModel> {
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate registrationDate;
    private ContactMethod preferredContact;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}