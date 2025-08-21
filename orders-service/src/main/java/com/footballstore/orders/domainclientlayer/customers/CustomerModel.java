package com.footballstore.orders.domainclientlayer.customers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerModel {
    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate registrationDate;
    private ContactMethod preferredContact;
    @JsonUnwrapped
    private Address address;
}
