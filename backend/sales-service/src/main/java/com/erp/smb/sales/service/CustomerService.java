package com.erp.smb.sales.service;

import com.erp.smb.sales.client.SearchIndexClient;
import com.erp.smb.sales.domain.Customer;
import com.erp.smb.sales.repo.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for customer operations.
 * Handles search indexing lifecycle hooks.
 */
@Service
public class CustomerService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    
    private final CustomerRepository customerRepository;
    private final SearchIndexClient searchIndexClient;
    
    public CustomerService(CustomerRepository customerRepository, SearchIndexClient searchIndexClient) {
        this.customerRepository = customerRepository;
        this.searchIndexClient = searchIndexClient;
    }
    
    /**
     * Create a customer and index it for search.
     * CREATE → index entity
     */
    @Transactional
    public Customer createCustomer(Customer customer, String tenantId) {
        Customer saved = customerRepository.save(customer);
        
        // Index in search service (non-blocking, failure-tolerant)
        indexCustomer(saved, tenantId);
        
        return saved;
    }
    
    /**
     * Update a customer and update search index.
     * UPDATE → update index
     */
    @Transactional
    public Customer updateCustomer(Customer customer, String tenantId) {
        Customer saved = customerRepository.save(customer);
        
        // Update search index (non-blocking, failure-tolerant)
        indexCustomer(saved, tenantId);
        
        return saved;
    }
    
    /**
     * Delete a customer and remove from search index.
     * DELETE → delete index
     */
    @Transactional
    public void deleteCustomer(Long id, String tenantId) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            String entityId = String.valueOf(id);
            customerRepository.deleteById(id);
            
            // Remove from search index (non-blocking, failure-tolerant)
            searchIndexClient.deleteCustomer(entityId, tenantId);
        }
    }
    
    /**
     * Find customer by ID
     */
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }
    
    /**
     * Index a customer in search service.
     * Private helper - failures are logged but don't propagate.
     */
    private void indexCustomer(Customer customer, String tenantId) {
        try {
            Map<String, Object> fields = new HashMap<>();
            fields.put("customerName", customer.getCustomerName());
            fields.put("email", customer.getEmail());
            fields.put("phone", customer.getPhone());
            fields.put("gstNumber", customer.getGstNumber());
            fields.put("address", customer.getAddress());
            
            String entityId = String.valueOf(customer.getId());
            searchIndexClient.indexCustomer(entityId, tenantId, fields);
            
        } catch (Exception e) {
            log.error("Error indexing customer: id={}, tenantId={}, error={}", 
                customer.getId(), tenantId, e.getMessage());
            // Don't propagate - search is a secondary concern
        }
    }
}
