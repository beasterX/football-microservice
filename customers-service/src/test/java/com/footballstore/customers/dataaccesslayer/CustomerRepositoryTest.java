package com.footballstore.customers.dataaccesslayer;

import com.footballstore.customers.dataaccesslayer.Customer;
import com.footballstore.customers.dataaccesslayer.CustomerIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    public void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    public void testSaveCustomer_thenCustomerIsPersisted() {
        Customer customer = Customer.builder()
                .customerIdentifier(new CustomerIdentifier("CUST001"))
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("1234567890")
                .registrationDate(LocalDate.now())
                .build();

        Customer savedCustomer = customerRepository.save(customer);

        assertNotNull(savedCustomer);
        assertNotNull(savedCustomer.getId(), "Saved customer should have a generated ID");
        assertEquals("CUST001", savedCustomer.getCustomerIdentifier().getCustomerId());
        assertEquals("John", savedCustomer.getFirstName());
    }

    @Test
    public void testFindByCustomerIdentifier_thenReturnCustomer() {
        Customer customer = Customer.builder()
                .customerIdentifier(new CustomerIdentifier("CUST002"))
                .firstName("Alice")
                .lastName("Smith")
                .email("alice.smith@example.com")
                .phone("9876543210")
                .registrationDate(LocalDate.now())
                .build();
        customerRepository.save(customer);

        Optional<Customer> found = customerRepository.findByCustomerIdentifier_CustomerId("CUST002");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getFirstName());
    }

    @Test
    public void testFindByNonExistentCustomerIdentifier_thenReturnEmptyOptional() {
        Optional<Customer> found = customerRepository.findByCustomerIdentifier_CustomerId("NON_EXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    public void testDeleteCustomer_thenRepositorySizeDecreases() {
        Customer customer = Customer.builder()
                .customerIdentifier(new CustomerIdentifier("CUST003"))
                .firstName("Bob")
                .lastName("Jones")
                .email("bob.jones@example.com")
                .registrationDate(LocalDate.now())
                .build();
        Customer saved = customerRepository.save(customer);
        customerRepository.delete(saved);
        List<Customer> allCustomers = customerRepository.findAll();
        assertEquals(0, allCustomers.size());
    }
}
