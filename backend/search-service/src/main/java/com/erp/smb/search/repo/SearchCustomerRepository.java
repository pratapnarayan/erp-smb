package com.erp.smb.search.repo;

import com.erp.smb.search.domain.SearchCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchCustomerRepository extends JpaRepository<SearchCustomer, Long> {
    
    /**
     * Full-text search using PostgreSQL tsvector
     */
    @Query(value = """
        SELECT c.*, ts_rank(c.search_vector, plainto_tsquery('english', :query)) as rank
        FROM search.search_customers c
        WHERE c.tenant_id = :tenantId
        AND c.search_vector @@ plainto_tsquery('english', :query)
        ORDER BY rank DESC, c.customer_name
        LIMIT :limit
        """, nativeQuery = true)
    List<SearchCustomer> fullTextSearch(
        @Param("tenantId") String tenantId,
        @Param("query") String query,
        @Param("limit") int limit
    );
    
    /**
     * Fuzzy search using pg_trgm similarity with threshold >= 0.3
     */
    @Query(value = """
        SELECT c.*, 
            GREATEST(
                similarity(c.customer_name, :query),
                similarity(COALESCE(c.email, ''), :query),
                similarity(COALESCE(c.phone, ''), :query)
            ) as similarity_score
        FROM search.search_customers c
        WHERE c.tenant_id = :tenantId
        AND (
            similarity(c.customer_name, :query) >= 0.3 OR
            similarity(COALESCE(c.email, ''), :query) >= 0.3 OR
            similarity(COALESCE(c.phone, ''), :query) >= 0.3
        )
        ORDER BY similarity_score DESC, c.customer_name
        LIMIT :limit
        """, nativeQuery = true)
    List<SearchCustomer> fuzzySearch(
        @Param("tenantId") String tenantId,
        @Param("query") String query,
        @Param("limit") int limit
    );
    
    /**
     * Autocomplete suggestions using prefix matching
     */
    @Query(value = """
        SELECT DISTINCT c.*
        FROM search.search_customers c
        WHERE c.tenant_id = :tenantId
        AND c.search_vector @@ to_tsquery('english', :prefixQuery)
        ORDER BY ts_rank(c.search_vector, to_tsquery('english', :prefixQuery)) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SearchCustomer> autocompleteSuggestions(
        @Param("tenantId") String tenantId,
        @Param("prefixQuery") String prefixQuery,
        @Param("limit") int limit
    );
    
    /**
     * Find by entity ID and tenant
     */
    Optional<SearchCustomer> findByTenantIdAndEntityId(String tenantId, String entityId);
    
    /**
     * Delete by entity ID and tenant
     */
    void deleteByTenantIdAndEntityId(String tenantId, String entityId);
    
    /**
     * Count by tenant
     */
    long countByTenantId(String tenantId);
}
