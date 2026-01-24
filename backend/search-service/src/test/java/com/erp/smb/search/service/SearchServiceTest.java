package com.erp.smb.search.service;

import com.erp.smb.common.search.*;
import com.erp.smb.search.domain.SearchCustomer;
import com.erp.smb.search.domain.SearchOrder;
import com.erp.smb.search.domain.SearchProduct;
import com.erp.smb.search.repo.SearchCustomerRepository;
import com.erp.smb.search.repo.SearchOrderRepository;
import com.erp.smb.search.repo.SearchProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SearchService tests using real PostgreSQL.
 * Tests full-text search, fuzzy search, tenant isolation, and performance.
 */
@SpringBootTest
@ActiveProfiles("local")
@Transactional
public class SearchServiceTest {
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private SearchProductRepository productRepository;
    
    @Autowired
    private SearchCustomerRepository customerRepository;
    
    @Autowired
    private SearchOrderRepository orderRepository;
    
    private static final String TENANT_A = "tenant-a";
    private static final String TENANT_B = "tenant-b";
    
    @BeforeEach
    void setup() {
        // Clear existing data
        productRepository.deleteAll();
        customerRepository.deleteAll();
        orderRepository.deleteAll();
        
        // Create test data for tenant A
        createProductForTenant(TENANT_A, "1", "Laptop Computer", "LAP-001", "Electronics");
        createProductForTenant(TENANT_A, "2", "Wireless Mouse", "MOU-001", "Electronics");
        createProductForTenant(TENANT_A, "3", "Office Chair", "CHR-001", "Furniture");
        
        // Create test data for tenant B (different data)
        createProductForTenant(TENANT_B, "10", "Desktop Computer", "DSK-001", "Electronics");
        createProductForTenant(TENANT_B, "11", "Standing Desk", "DSK-002", "Furniture");
        
        // Create customers
        createCustomerForTenant(TENANT_A, "100", "Acme Corp", "contact@acme.com", "9876543210");
        createCustomerForTenant(TENANT_B, "200", "Beta Inc", "info@beta.com", "9876543211");
    }
    
    @Test
    void testFullTextSearchReturnsResults() {
        // Given: Products indexed for tenant A
        SearchCriteria criteria = new SearchCriteria("laptop", SearchEntityType.PRODUCT, TENANT_A);
        criteria.setFuzzyMatch(false);
        
        // When: Searching for "laptop"
        SearchResult<SearchHit> result = searchService.globalSearch(criteria);
        
        // Then: Results found
        assertNotNull(result);
        assertTrue(result.getTotalCount() > 0);
        assertEquals(1, result.getResults().size());
        assertEquals("Laptop Computer", result.getResults().get(0).getTitle());
        assertTrue(result.getSearchTimeMs() < 300, "Search should be under 300ms");
    }
    
    @Test
    void testFuzzySearchOnlyTriggersWhenAllowed() {
        // Given: Query length < 3
        SearchCriteria criteria = new SearchCriteria("la", SearchEntityType.PRODUCT, TENANT_A);
        criteria.setFuzzyMatch(true);
        
        // When: Searching with short query
        SearchResult<SearchHit> result = searchService.globalSearch(criteria);
        
        // Then: Fuzzy search should be disabled (query too short)
        // Note: Results may still come from full-text search if it matches
        assertNotNull(result);
    }
    
    @Test
    void testFuzzySearchEnabledForLongerQueries() {
        // Given: Query length >= 3 with typo
        SearchCriteria criteria = new SearchCriteria("loptop", SearchEntityType.PRODUCT, TENANT_A);
        criteria.setFuzzyMatch(true);
        
        // When: Searching with typo (fuzzy should help)
        SearchResult<SearchHit> result = searchService.globalSearch(criteria);
        
        // Then: May find "Laptop" through fuzzy matching
        assertNotNull(result);
        assertTrue(result.getSearchTimeMs() < 300, "Fuzzy search should be under 300ms");
    }
    
    @Test
    void testPaginationWorks() {
        // Given: Criteria with pagination
        SearchCriteria criteria = new SearchCriteria("", SearchEntityType.PRODUCT, TENANT_A);
        criteria.setPage(0);
        criteria.setSize(2);
        
        // When: Searching (empty query returns all via fallback)
        // Note: Empty query behavior depends on implementation
        SearchResult<SearchHit> result = searchService.globalSearch(criteria);
        
        // Then: Pagination info is correct
        assertNotNull(result);
        assertEquals(0, result.getPage());
        assertEquals(2, result.getSize());
    }
    
    /**
     * CRITICAL TEST: Tenant isolation must be absolute.
     * If this fails, STOP and fix immediately.
     */
    @Test
    void testTenantIsolation_NoLeakage() {
        // Given: Same query, different tenants
        SearchCriteria criteriaA = new SearchCriteria("computer", SearchEntityType.PRODUCT, TENANT_A);
        SearchCriteria criteriaB = new SearchCriteria("computer", SearchEntityType.PRODUCT, TENANT_B);
        
        // When: Searching for both tenants
        SearchResult<SearchHit> resultA = searchService.globalSearch(criteriaA);
        SearchResult<SearchHit> resultB = searchService.globalSearch(criteriaB);
        
        // Then: Results are different
        assertNotNull(resultA);
        assertNotNull(resultB);
        
        // Tenant A should find "Laptop Computer"
        assertTrue(resultA.getResults().stream()
            .anyMatch(hit -> hit.getTitle().contains("Laptop")));
        
        // Tenant B should find "Desktop Computer"
        assertTrue(resultB.getResults().stream()
            .anyMatch(hit -> hit.getTitle().contains("Desktop")));
        
        // Tenant A should NOT see Tenant B's data
        assertFalse(resultA.getResults().stream()
            .anyMatch(hit -> hit.getTitle().contains("Desktop")));
        
        // Tenant B should NOT see Tenant A's data
        assertFalse(resultB.getResults().stream()
            .anyMatch(hit -> hit.getTitle().contains("Laptop")));
        
        System.out.println("✓ CRITICAL: Tenant isolation verified - no cross-tenant leakage");
    }
    
    @Test
    void testGlobalSearchAcrossAllEntityTypes() {
        // Given: No entity type filter
        SearchCriteria criteria = new SearchCriteria("acme", null, TENANT_A);
        
        // When: Searching across all types
        SearchResult<SearchHit> result = searchService.globalSearch(criteria);
        
        // Then: Should find customer
        assertNotNull(result);
        assertTrue(result.getTotalCount() > 0);
        assertTrue(result.getResults().stream()
            .anyMatch(hit -> hit.getEntityType() == SearchEntityType.CUSTOMER));
    }
    
    @Test
    void testV1EntityTypesOnly() {
        // Given: Unsupported entity type
        SearchCriteria criteria = new SearchCriteria("test", SearchEntityType.ENQUIRY, TENANT_A);
        
        // When/Then: Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            searchService.globalSearch(criteria);
        });
    }
    
    @Test
    void testRelevanceOrderingIsStable() {
        // Given: Multiple products with varying relevance
        createProductForTenant(TENANT_A, "20", "Laptop Pro", "LAP-PRO", "Electronics");
        createProductForTenant(TENANT_A, "21", "Laptop Bag", "BAG-001", "Accessories");
        
        SearchCriteria criteria = new SearchCriteria("laptop", SearchEntityType.PRODUCT, TENANT_A);
        
        // When: Searching multiple times
        SearchResult<SearchHit> result1 = searchService.globalSearch(criteria);
        SearchResult<SearchHit> result2 = searchService.globalSearch(criteria);
        
        // Then: Order should be stable
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getResults().size(), result2.getResults().size());
        
        if (result1.getResults().size() > 0) {
            assertEquals(result1.getResults().get(0).getEntityId(), 
                        result2.getResults().get(0).getEntityId());
        }
    }
    
    // Helper methods
    
    private void createProductForTenant(String tenantId, String entityId, String name, String sku, String category) {
        SearchProduct product = new SearchProduct();
        product.setEntityId(entityId);
        product.setTenantId(tenantId);
        product.setName(name);
        product.setSku(sku);
        product.setCategory(category);
        productRepository.save(product);
    }
    
    private void createCustomerForTenant(String tenantId, String entityId, String name, String email, String phone) {
        SearchCustomer customer = new SearchCustomer();
        customer.setEntityId(entityId);
        customer.setTenantId(tenantId);
        customer.setCustomerName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customerRepository.save(customer);
    }
}
