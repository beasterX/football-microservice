package com.footballstore.apigateway.businesslayer.customers;

import com.footballstore.apigateway.presentationlayer.customers.CustomerRequestModel;
import com.footballstore.apigateway.presentationlayer.customers.CustomerResponseModel;
import java.util.List;

public interface CustomersService {
    List<CustomerResponseModel> getAllCustomers();
    CustomerResponseModel getCustomerById(String customerId);
    CustomerResponseModel createCustomer(CustomerRequestModel customerRequestModel);
    CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel customerRequestModel);
    CustomerResponseModel deleteCustomer(String customerId);
}
