package com.erp.smb.product.service;

import com.erp.smb.product.client.SearchIndexClient;
import com.erp.smb.product.domain.Item;
import com.erp.smb.product.repo.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for product operations.
 * Handles search indexing lifecycle hooks.
 */
@Service
public class ProductService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    
    private final ItemRepository itemRepository;
    private final SearchIndexClient searchIndexClient;
    
    public ProductService(ItemRepository itemRepository, SearchIndexClient searchIndexClient) {
        this.itemRepository = itemRepository;
        this.searchIndexClient = searchIndexClient;
    }
    
    /**
     * Create a product and index it for search.
     * CREATE → index entity
     */
    @Transactional
    public Item createProduct(Item item, String tenantId) {
        Item saved = itemRepository.save(item);
        
        // Index in search service (non-blocking, failure-tolerant)
        indexProduct(saved, tenantId);
        
        return saved;
    }
    
    /**
     * Update a product and update search index.
     * UPDATE → update index
     */
    @Transactional
    public Item updateProduct(Item item, String tenantId) {
        Item saved = itemRepository.save(item);
        
        // Update search index (non-blocking, failure-tolerant)
        indexProduct(saved, tenantId);
        
        return saved;
    }
    
    /**
     * Delete a product and remove from search index.
     * DELETE → delete index
     */
    @Transactional
    public void deleteProduct(Long id, String tenantId) {
        Optional<Item> item = itemRepository.findById(id);
        if (item.isPresent()) {
            String entityId = String.valueOf(id);
            itemRepository.deleteById(id);
            
            // Remove from search index (non-blocking, failure-tolerant)
            searchIndexClient.deleteProduct(entityId, tenantId);
        }
    }
    
    /**
     * Find product by ID
     */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }
    
    /**
     * Index a product in search service.
     * Private helper - failures are logged but don't propagate.
     */
    private void indexProduct(Item item, String tenantId) {
        try {
            Map<String, Object> fields = new HashMap<>();
            fields.put("name", item.getName());
            fields.put("sku", item.getSku());
            fields.put("category", item.getCategory());
            fields.put("description", ""); // Item doesn't have description field
            
            String entityId = String.valueOf(item.getId());
            searchIndexClient.indexProduct(entityId, tenantId, fields);
            
        } catch (Exception e) {
            log.error("Error indexing product: id={}, tenantId={}, error={}", 
                item.getId(), tenantId, e.getMessage());
            // Don't propagate - search is a secondary concern
        }
    }
}
