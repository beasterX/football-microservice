package com.footballstore.customers.businesslayer;

import com.footballstore.customers.dataaccesslayer.Address;
import com.footballstore.customers.dataaccesslayer.Customer;
import com.footballstore.customers.dataaccesslayer.CustomerIdentifier;
import com.footballstore.customers.dataaccesslayer.CustomerRepository;
import com.footballstore.customers.datamappinglayer.CustomerRequestMapper;
import com.footballstore.customers.datamappinglayer.CustomerResponseMapper;
import com.footballstore.customers.presentationlayer.CustomerRequestModel;
import com.footballstore.customers.presentationlayer.CustomerResponseModel;
import com.footballstore.customers.utils.exceptions.DuplicateEmailException;
import com.footballstore.customers.utils.exceptions.InvalidInputException;
import com.footballstore.customers.utils.exceptions.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerResponseMapper customerResponseMapper;
    private final CustomerRequestMapper customerRequestMapper;

    @Override
    public List<CustomerResponseModel> getAllCustomers() {
        return customerResponseMapper.entityListToResponseModelList(customerRepository.findAll());
    }

    @Override
    public CustomerResponseModel getCustomerById(String customerId) {
        validateUuid(customerId);
        Customer customer = customerRepository
                .findByCustomerIdentifier_CustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + customerId));
        return customerResponseMapper.entityToResponseModel(customer);
    }

    @Override
    public CustomerResponseModel createCustomer(CustomerRequestModel request) {
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Customer with email " + request.getEmail() + " already exists.");
        }
        Customer c = customerRequestMapper.requestModelToEntity(request);
        c.setCustomerIdentifier(CustomerIdentifier.generate());
        c.setAddress(Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build());
        c.setRegistrationDate(LocalDate.now());
        return customerResponseMapper.entityToResponseModel(customerRepository.save(c));
    }

    @Override
    public CustomerResponseModel updateCustomer(String customerId, CustomerRequestModel request) {
        validateUuid(customerId);

        Customer existing = customerRepository
                .findByCustomerIdentifier_CustomerId(customerId)
                .orElseThrow(() ->
                        new NotFoundException("Customer not found with id: " + customerId)
                );

        customerRepository.findByEmail(request.getEmail())
                .filter(c -> !c.getCustomerIdentifier().getCustomerId().equals(customerId))
                .ifPresent(c -> {
                    throw new DuplicateEmailException(
                            "Customer with email " + request.getEmail() + " already exists.");
                });

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setEmail(request.getEmail());
        existing.setPhone(request.getPhone());
        existing.setPreferredContact(request.getPreferredContact());
        existing.setAddress(Address.builder()
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .build()
        );

        Customer saved = customerRepository.save(existing);
        return customerResponseMapper.entityToResponseModel(saved);
    }


    @Override
    public void deleteCustomer(String customerId) {
        validateUuid(customerId);
        Customer existing = customerRepository
                .findByCustomerIdentifier_CustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + customerId));
        customerRepository.delete(existing);
    }

    private void validateUuid(String id) {
        try {
            UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException("Provided customerId is invalid: " + id);
        }
    }
}
