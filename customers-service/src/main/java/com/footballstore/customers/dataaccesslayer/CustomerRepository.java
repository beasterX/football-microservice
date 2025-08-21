package com.footballstore.customers.dataaccesslayer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByCustomerIdentifier_CustomerId(String customerId);

    Optional<Customer> findByEmail(String email);


}