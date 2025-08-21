package com.footballstore.customers.businesslayer;



import com.footballstore.customers.presentationlayer.CustomerRequestModel;
import com.footballstore.customers.presentationlayer.CustomerResponseModel;

import java.util.List;

public interface CustomerService {
    List<CustomerResponseModel> getAllCustomers();
    CustomerResponseModel getCustomerById(String customerId);
    CustomerResponseModel createCustomer(CustomerRequestModel customerRequestModel);
    CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel customerRequestModel);
    void deleteCustomer(String customerId);
}