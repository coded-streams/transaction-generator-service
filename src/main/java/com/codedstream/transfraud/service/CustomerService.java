package com.codedstream.transfraud.service;

import com.codedstream.transfraud.model.entity.Customer;
import com.codedstream.transfraud.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(String id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomer(String id) {
        customerRepository.deleteById(id);
    }

    public long getCustomerCount() {
        return customerRepository.count();
    }

    public List<Customer> getCustomersWithCards() {
        return customerRepository.findCustomersWithCards();
    }
}
