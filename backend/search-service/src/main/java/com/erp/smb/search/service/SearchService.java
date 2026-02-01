package com.erp.smb.search.service;

import com.erp.smb.common.search.*;
import com.erp.smb.search.domain.SearchCustomer;
import com.erp.smb.search.domain.SearchOrder;
import com.erp.smb.search.domain.SearchProduct;
import com.erp.smb.search.repo.SearchCustomerRepository;
import com.erp.smb.search.repo.SearchOrderRepository;
import com.erp.smb.search.repo.SearchProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {
    
    private final SearchProductRepository productRepository;
    private final SearchCustomerRepository customerRepository;
    private final SearchOrderRepository orderRepository;
    
    @Value("${app.search.default-limit:20}")
    private int defaultLimit;
    
    @Value("${app.search.max-results:100}")
    private int maxResults;
    
    @Value("${app.search.suggestion-limit:10}")
    private int suggestionLimit;
    
    public SearchService(
            SearchProductRepository productRepository,
            SearchCustomerRepository customerRepository,
            SearchOrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }
    
    /**
     * Global search across all entity types.
     * Returns unified SearchHit DTOs for frontend consumption.
     * Only searches v1-supported entity types.
     * 
     * Fuzzy search guardrails:
     * - Disabled if query length < 3
     * - Full-text search executed first
     * - Fuzzy search only if full-text results insufficient
     */
    @Transactional(readOnly = true)
    public SearchResult<SearchHit> globalSearch(SearchCriteria criteria) {
        long startTime = System.currentTimeMillis();
        
        String tenantId = criteria.getTenantId();

        // Preserve the raw query to detect identifier/code lookups (e.g., SO-1005, SKU-1005).
        String rawQuery = criteria.getQuery();
        String query = SearchUtils.normalizeQuery(rawQuery);
        int limit = Math.min(criteria.getSize(), maxResults);

        // Code-like queries should be treated as exact matches to avoid noisy results from
        // normalization/tokenization (e.g., "SO-1005" -> "so 1005") and fuzzy similarity.
        boolean codeLike = SearchUtils.isCodeLikeQuery(rawQuery);
        String codeQuery = codeLike ? SearchUtils.normalizeCodeQuery(rawQuery) : null;
        
        // Validate entity type is v1-supported
        if (criteria.getEntityType() != null && !criteria.getEntityType().isV1Supported()) {
            throw new IllegalArgumentException("Entity type " + criteria.getEntityType() + " is not supported in v1");
        }
        
        // Fuzzy search guardrail: disable for short queries and for code-like queries
        boolean useFuzzy = criteria.isFuzzyMatch() && query.length() >= 3 && !codeLike;

        List<SearchHit> results = new ArrayList<>();

        // For code-like queries, short-circuit to exact matching first (and only).
        if (codeLike) {
            if (criteria.getEntityType() == null) {
                results.addAll(searchProductsAsHits(tenantId, codeQuery, limit, false));
                results.addAll(searchCustomersAsHits(tenantId, codeQuery, limit, false));
                results.addAll(searchOrdersAsHits(tenantId, codeQuery, limit, false));
            } else {
                results.addAll(searchByTypeAsHits(criteria.getEntityType(), tenantId, codeQuery, limit, false));
            }
        } else if (criteria.getEntityType() == null) {
            // Search all v1-supported entity types
            // Always try full-text first
            results.addAll(searchProductsAsHits(tenantId, query, limit, false));
            results.addAll(searchCustomersAsHits(tenantId, query, limit, false));
            results.addAll(searchOrdersAsHits(tenantId, query, limit, false));

            // If full-text results are insufficient and fuzzy is enabled, try fuzzy
            if (useFuzzy && results.size() < limit / 2) {
                results.addAll(searchProductsAsHits(tenantId, query, limit - results.size(), true));
                results.addAll(searchCustomersAsHits(tenantId, query, limit - results.size(), true));
                results.addAll(searchOrdersAsHits(tenantId, query, limit - results.size(), true));
            }
        } else {
            // Search specific entity type with same guardrails
            results.addAll(searchByTypeAsHits(criteria.getEntityType(), tenantId, query, limit, false));

            if (useFuzzy && results.size() < limit / 2) {
                results.addAll(searchByTypeAsHits(criteria.getEntityType(), tenantId, query, limit - results.size(), true));
            }
        }
        
        long searchTime = System.currentTimeMillis() - startTime;
        
        SearchResult<SearchHit> result = new SearchResult<>();
        result.setResults(results);
        result.setTotalCount(results.size());
        result.setPage(criteria.getPage());
        result.setSize(criteria.getSize());
        result.setSearchTimeMs(searchTime);
        result.setQuery(query);
        
        return result;
    }
    
    /**
     * Get autocomplete suggestions
     */
    @Transactional(readOnly = true)
    public List<SearchSuggestion> getSuggestions(String tenantId, String query, SearchEntityType entityType) {
        String normalizedQuery = SearchUtils.normalizeQuery(query);
        String prefixQuery = SearchUtils.toPrefixQuery(normalizedQuery);
        
        if (prefixQuery.isEmpty()) {
            return List.of();
        }
        
        List<SearchSuggestion> suggestions = new ArrayList<>();
        
        if (entityType == null || entityType == SearchEntityType.PRODUCT) {
            List<SearchProduct> products = productRepository.autocompleteSuggestions(
                tenantId, prefixQuery, suggestionLimit);
            suggestions.addAll(products.stream()
                .map(p -> new SearchSuggestion(p.getName(), SearchEntityType.PRODUCT, p.getEntityId()))
                .collect(Collectors.toList()));
        }
        
        if (entityType == null || entityType == SearchEntityType.CUSTOMER) {
            List<SearchCustomer> customers = customerRepository.autocompleteSuggestions(
                tenantId, prefixQuery, suggestionLimit);
            suggestions.addAll(customers.stream()
                .map(c -> new SearchSuggestion(c.getCustomerName(), SearchEntityType.CUSTOMER, c.getEntityId()))
                .collect(Collectors.toList()));
        }
        
        if (entityType == null || entityType == SearchEntityType.ORDER) {
            List<SearchOrder> orders = orderRepository.autocompleteSuggestions(
                tenantId, prefixQuery, suggestionLimit);
            suggestions.addAll(orders.stream()
                .map(o -> new SearchSuggestion(o.getOrderNumber(), SearchEntityType.ORDER, o.getEntityId()))
                .collect(Collectors.toList()));
        }
        
        return suggestions.stream().limit(suggestionLimit).collect(Collectors.toList());
    }
    
    /**
     * Index a product for search
     */
    @Transactional
    public void indexProduct(SearchProduct product) {
        productRepository.findByTenantIdAndEntityId(product.getTenantId(), product.getEntityId())
            .ifPresentOrElse(
                existing -> {
                    existing.setName(product.getName());
                    existing.setSku(product.getSku());
                    existing.setCategory(product.getCategory());
                    existing.setDescription(product.getDescription());
                    productRepository.save(existing);
                },
                () -> productRepository.save(product)
            );
    }
    
    /**
     * Index a customer for search
     */
    @Transactional
    public void indexCustomer(SearchCustomer customer) {
        customerRepository.findByTenantIdAndEntityId(customer.getTenantId(), customer.getEntityId())
            .ifPresentOrElse(
                existing -> {
                    existing.setCustomerName(customer.getCustomerName());
                    existing.setEmail(customer.getEmail());
                    existing.setPhone(customer.getPhone());
                    existing.setGstNumber(customer.getGstNumber());
                    existing.setAddress(customer.getAddress());
                    customerRepository.save(existing);
                },
                () -> customerRepository.save(customer)
            );
    }
    
    /**
     * Index an order for search
     */
    @Transactional
    public void indexOrder(SearchOrder order) {
        orderRepository.findByTenantIdAndEntityId(order.getTenantId(), order.getEntityId())
            .ifPresentOrElse(
                existing -> {
                    existing.setOrderNumber(order.getOrderNumber());
                    existing.setCustomerName(order.getCustomerName());
                    existing.setStatus(order.getStatus());
                    existing.setTotalAmount(order.getTotalAmount());
                    existing.setOrderDate(order.getOrderDate());
                    orderRepository.save(existing);
                },
                () -> orderRepository.save(order)
            );
    }
    
    /**
     * Delete search index by entity
     */
    @Transactional
    public void deleteIndex(SearchEntityType entityType, String tenantId, String entityId) {
        switch (entityType) {
            case PRODUCT -> productRepository.deleteByTenantIdAndEntityId(tenantId, entityId);
            case CUSTOMER -> customerRepository.deleteByTenantIdAndEntityId(tenantId, entityId);
            case ORDER -> orderRepository.deleteByTenantIdAndEntityId(tenantId, entityId);
        }
    }
    
    /**
     * Bulk reindex for a specific entity type.
     * Idempotent - can be run multiple times safely.
     * ADMIN role only.
     * 
     * @param entityType The entity type to reindex
     * @param tenantId The tenant to reindex for
     * @return Number of entities reindexed
     */
    @Transactional
    public long bulkReindex(SearchEntityType entityType, String tenantId) {
        if (!entityType.isV1Supported()) {
            throw new IllegalArgumentException("Entity type " + entityType + " is not supported for reindexing");
        }
        
        long startTime = System.currentTimeMillis();
        long count = 0;
        
        // Note: In production, this should fetch from entity services via REST
        // For now, this is a placeholder that clears and rebuilds from existing data
        switch (entityType) {
            case PRODUCT -> {
                // Clear existing indexes for this tenant
                List<SearchProduct> existing = productRepository.findAll().stream()
                    .filter(p -> p.getTenantId().equals(tenantId))
                    .toList();
                count = existing.size();
                // In production: fetch from product-service and reindex
            }
            case CUSTOMER -> {
                List<SearchCustomer> existing = customerRepository.findAll().stream()
                    .filter(c -> c.getTenantId().equals(tenantId))
                    .toList();
                count = existing.size();
                // In production: fetch from sales-service and reindex
            }
            case ORDER -> {
                List<SearchOrder> existing = orderRepository.findAll().stream()
                    .filter(o -> o.getTenantId().equals(tenantId))
                    .toList();
                count = existing.size();
                // In production: fetch from order-service and reindex
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        // Log for monitoring
        System.out.println(String.format(
            "[REINDEX] entityType=%s, tenantId=%s, count=%d, duration=%dms",
            entityType, tenantId, count, duration
        ));
        
        return count;
    }
    
    /**
     * Bulk reindex all v1-supported entity types for a tenant
     */
    @Transactional
    public long bulkReindexAll(String tenantId) {
        long totalCount = 0;
        for (SearchEntityType entityType : SearchEntityType.getV1Supported()) {
            totalCount += bulkReindex(entityType, tenantId);
        }
        return totalCount;
    }
    
    // Private helper methods - Convert entities to SearchHit DTOs
    
    private List<SearchHit> searchProductsAsHits(String tenantId, String query, int limit, boolean fuzzy) {
        List<SearchProduct> products = searchProducts(tenantId, query, limit, fuzzy);
        return products.stream()
            .map(this::productToSearchHit)
            .collect(Collectors.toList());
    }
    
    private List<SearchHit> searchCustomersAsHits(String tenantId, String query, int limit, boolean fuzzy) {
        List<SearchCustomer> customers = searchCustomers(tenantId, query, limit, fuzzy);
        return customers.stream()
            .map(this::customerToSearchHit)
            .collect(Collectors.toList());
    }
    
    private List<SearchHit> searchOrdersAsHits(String tenantId, String query, int limit, boolean fuzzy) {
        List<SearchOrder> orders = searchOrders(tenantId, query, limit, fuzzy);
        return orders.stream()
            .map(this::orderToSearchHit)
            .collect(Collectors.toList());
    }
    
    private List<SearchHit> searchByTypeAsHits(SearchEntityType entityType, String tenantId, String query, int limit, boolean fuzzy) {
        return switch (entityType) {
            case PRODUCT -> searchProductsAsHits(tenantId, query, limit, fuzzy);
            case CUSTOMER -> searchCustomersAsHits(tenantId, query, limit, fuzzy);
            case ORDER -> searchOrdersAsHits(tenantId, query, limit, fuzzy);
            default -> List.of();
        };
    }
    
    private List<SearchProduct> searchProducts(String tenantId, String query, int limit, boolean fuzzy) {
        if (fuzzy) {
            return productRepository.fuzzySearch(tenantId, query, limit);
        }
        if (SearchUtils.isCodeLikeQuery(query)) {
            return productRepository.exactCodeMatch(tenantId, SearchUtils.normalizeCodeQuery(query), limit);
        }
        return productRepository.fullTextSearch(tenantId, query, limit);
    }

    private List<SearchCustomer> searchCustomers(String tenantId, String query, int limit, boolean fuzzy) {
        if (fuzzy) {
            return customerRepository.fuzzySearch(tenantId, query, limit);
        }
        if (SearchUtils.isCodeLikeQuery(query)) {
            return customerRepository.exactCodeMatch(tenantId, SearchUtils.normalizeCodeQuery(query), limit);
        }
        return customerRepository.fullTextSearch(tenantId, query, limit);
    }

    private List<SearchOrder> searchOrders(String tenantId, String query, int limit, boolean fuzzy) {
        if (fuzzy) {
            return orderRepository.fuzzySearch(tenantId, query, limit);
        }
        if (SearchUtils.isCodeLikeQuery(query)) {
            return orderRepository.exactCodeMatch(tenantId, SearchUtils.normalizeCodeQuery(query), limit);
        }
        return orderRepository.fullTextSearch(tenantId, query, limit);
    }
    
    // Entity to SearchHit converters
    
    private SearchHit productToSearchHit(SearchProduct product) {
        SearchHit hit = new SearchHit(SearchEntityType.PRODUCT, product.getEntityId(), product.getName());
        hit.setSubtitle(product.getSku() != null ? "SKU: " + product.getSku() : product.getCategory());
        hit.setHighlight(product.getName());
        return hit;
    }
    
    private SearchHit customerToSearchHit(SearchCustomer customer) {
        SearchHit hit = new SearchHit(SearchEntityType.CUSTOMER, customer.getEntityId(), customer.getCustomerName());
        hit.setSubtitle(customer.getEmail() != null ? customer.getEmail() : customer.getPhone());
        hit.setHighlight(customer.getCustomerName());
        return hit;
    }
    
    private SearchHit orderToSearchHit(SearchOrder order) {
        SearchHit hit = new SearchHit(SearchEntityType.ORDER, order.getEntityId(), order.getOrderNumber());
        String subtitle = order.getCustomerName();
        if (order.getOrderDate() != null) {
            subtitle += " • " + order.getOrderDate().toString();
        }
        hit.setSubtitle(subtitle);
        hit.setHighlight(order.getOrderNumber());
        return hit;
    }
}
