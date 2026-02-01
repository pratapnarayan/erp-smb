package com.erp.smb.search.web;

import com.erp.smb.common.search.SearchDocument;
import com.erp.smb.common.search.SearchEntityType;
import com.erp.smb.search.domain.SearchCustomer;
import com.erp.smb.search.domain.SearchOrder;
import com.erp.smb.search.domain.SearchProduct;
import com.erp.smb.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/search/index")
@Tag(name = "Search Index", description = "Indexing operations for search service")
@SecurityRequirement(name = "bearer-jwt")
public class SearchIndexController {
    
    private final SearchService searchService;
    
    public SearchIndexController(SearchService searchService) {
        this.searchService = searchService;
    }
    
    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Index a product", description = "Add or update product in search index. ADMIN role only.")
    public ResponseEntity<Void> indexProduct(@RequestBody SearchDocument document) {
        SearchProduct product = new SearchProduct();
        product.setEntityId(document.getEntityId());
        product.setTenantId(document.getTenantId());
        product.setName((String) document.getSearchableFields().get("name"));
        product.setSku((String) document.getSearchableFields().get("sku"));
        product.setCategory((String) document.getSearchableFields().get("category"));
        product.setDescription((String) document.getSearchableFields().get("description"));
        
        searchService.indexProduct(product);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Index a customer", description = "Add or update customer in search index. ADMIN role only.")
    public ResponseEntity<Void> indexCustomer(@RequestBody SearchDocument document) {
        SearchCustomer customer = new SearchCustomer();
        customer.setEntityId(document.getEntityId());
        customer.setTenantId(document.getTenantId());
        customer.setCustomerName((String) document.getSearchableFields().get("customerName"));
        customer.setEmail((String) document.getSearchableFields().get("email"));
        customer.setPhone((String) document.getSearchableFields().get("phone"));
        customer.setGstNumber((String) document.getSearchableFields().get("gstNumber"));
        customer.setAddress((String) document.getSearchableFields().get("address"));
        
        searchService.indexCustomer(customer);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Index an order", description = "Add or update order in search index. ADMIN role only.")
    public ResponseEntity<Void> indexOrder(@RequestBody SearchDocument document) {
        SearchOrder order = new SearchOrder();
        order.setEntityId(document.getEntityId());
        order.setTenantId(document.getTenantId());
        order.setOrderNumber((String) document.getSearchableFields().get("orderNumber"));
        order.setCustomerName((String) document.getSearchableFields().get("customerName"));
        order.setStatus((String) document.getSearchableFields().get("status"));
        
        Object totalAmount = document.getSearchableFields().get("totalAmount");
        if (totalAmount != null) {
            order.setTotalAmount(new BigDecimal(totalAmount.toString()));
        }
        
        Object orderDate = document.getSearchableFields().get("orderDate");
        if (orderDate != null) {
            order.setOrderDate(LocalDate.parse(orderDate.toString()));
        }
        
        searchService.indexOrder(order);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete from index", description = "Remove entity from search index. ADMIN role only.")
    public ResponseEntity<Void> deleteFromIndex(
            @PathVariable SearchEntityType entityType,
            @PathVariable String entityId,
            @RequestHeader("X-Tenant-Id") String tenantId) {
        
        searchService.deleteIndex(entityType, tenantId, entityId);
        return ResponseEntity.ok().build();
    }
}
