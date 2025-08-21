package com.footballstore.customers.datamappinglayer;

import com.footballstore.customers.dataaccesslayer.Customer;
import com.footballstore.customers.presentationlayer.CustomerController;
import com.footballstore.customers.presentationlayer.CustomerResponseModel;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Mapper(componentModel = "spring")
public interface CustomerResponseMapper {

    @Mapping(source = "customerIdentifier.customerId", target = "customerId")
    @Mapping(source = "address.street", target = "street")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.state", target = "state")
    @Mapping(source = "address.postalCode", target = "postalCode")
    @Mapping(source = "address.country", target = "country")
    CustomerResponseModel entityToResponseModel(Customer customer);

    List<CustomerResponseModel> entityListToResponseModelList(List<Customer> customers);

}