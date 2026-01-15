package com.erp.smb.sales.repo;

import com.erp.smb.sales.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByEmail(String email);
  Optional<Customer> findByCustomerName(String customerName);
}
