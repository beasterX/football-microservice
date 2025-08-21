package com.footballstore.apigateway.presentationlayer.customers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponseModel extends RepresentationModel<CustomerResponseModel> {
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String registrationDate;
    private String preferredContact;
    private String street;
    private String city;
    private String state;
    private String postalCode;
    private String country;
}
