package com.footballstore.customers.datamappinglayer;

import com.footballstore.customers.dataaccesslayer.Customer;
import com.footballstore.customers.presentationlayer.CustomerRequestModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CustomerRequestMapper {
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "customerIdentifier", ignore = true),
            @Mapping(target = "address", ignore = true),
            @Mapping(target = "registrationDate", ignore = true)
    })
    Customer requestModelToEntity(CustomerRequestModel requestModel);
}