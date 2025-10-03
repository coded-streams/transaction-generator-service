package com.codedstream.transfraud.repository;

import com.codedstream.transfraud.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE SIZE(c.cards) > 0")
    List<Customer> findCustomersWithCards();

    long countByAddressCity(String city);
}
