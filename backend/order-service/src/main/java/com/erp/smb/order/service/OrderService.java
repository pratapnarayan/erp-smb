package com.erp.smb.order.service;

import com.erp.smb.order.client.SearchIndexClient;
import com.erp.smb.order.domain.SalesOrder;
import com.erp.smb.order.repo.SalesOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for order operations.
 * Handles search indexing lifecycle hooks.
 */
@Service
public class OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    private final SalesOrderRepository orderRepository;
    private final SearchIndexClient searchIndexClient;
    
    public OrderService(SalesOrderRepository orderRepository, SearchIndexClient searchIndexClient) {
        this.orderRepository = orderRepository;
        this.searchIndexClient = searchIndexClient;
    }
    
    /**
     * Create an order and index it for search.
     * CREATE → index entity
     */
    @Transactional
    public SalesOrder createOrder(SalesOrder order, String tenantId) {
        SalesOrder saved = orderRepository.save(order);
        
        // Index in search service (non-blocking, failure-tolerant)
        indexOrder(saved, tenantId);
        
        return saved;
    }
    
    /**
     * Update an order and update search index.
     * UPDATE → update index
     */
    @Transactional
    public SalesOrder updateOrder(SalesOrder order, String tenantId) {
        SalesOrder saved = orderRepository.save(order);
        
        // Update search index (non-blocking, failure-tolerant)
        indexOrder(saved, tenantId);
        
        return saved;
    }
    
    /**
     * Delete an order and remove from search index.
     * DELETE → delete index
     */
    @Transactional
    public void deleteOrder(Long id, String tenantId) {
        Optional<SalesOrder> order = orderRepository.findById(id);
        if (order.isPresent()) {
            String entityId = String.valueOf(id);
            orderRepository.deleteById(id);
            
            // Remove from search index (non-blocking, failure-tolerant)
            searchIndexClient.deleteOrder(entityId, tenantId);
        }
    }
    
    /**
     * Find order by ID
     */
    public Optional<SalesOrder> findById(Long id) {
        return orderRepository.findById(id);
    }
    
    /**
     * Index an order in search service.
     * Private helper - failures are logged but don't propagate.
     */
    private void indexOrder(SalesOrder order, String tenantId) {
        try {
            Map<String, Object> fields = new HashMap<>();
            fields.put("orderNumber", order.getCode());        // SalesOrder uses 'code' field
            fields.put("customerName", order.getCustomer());   // SalesOrder uses 'customer' field
            fields.put("status", order.getStatus());
            fields.put("totalAmount", order.getTotal());       // SalesOrder uses 'total' field
            fields.put("orderDate", order.getOrderDate() != null ? order.getOrderDate().toString() : null);
            
            String entityId = String.valueOf(order.getId());
            searchIndexClient.indexOrder(entityId, tenantId, fields);
            
        } catch (Exception e) {
            log.error("Error indexing order: id={}, tenantId={}, error={}", 
                order.getId(), tenantId, e.getMessage());
            // Don't propagate - search is a secondary concern
        }
    }
}
