package com.erp.smb.search.repo;

import com.erp.smb.search.domain.SearchOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchOrderRepository extends JpaRepository<SearchOrder, Long> {
    
    /**
     * Full-text search using PostgreSQL tsvector
     */
    @Query(value = """
        SELECT o.*, ts_rank(o.search_vector, plainto_tsquery('english', :query)) as rank
        FROM search.search_orders o
        WHERE o.tenant_id = :tenantId
        AND o.search_vector @@ plainto_tsquery('english', :query)
        ORDER BY rank DESC, o.order_date DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SearchOrder> fullTextSearch(
        @Param("tenantId") String tenantId,
        @Param("query") String query,
        @Param("limit") int limit
    );
    
    /**
     * Fuzzy search using pg_trgm similarity with threshold >= 0.3
     */
    @Query(value = """
        SELECT o.*, 
            GREATEST(
                similarity(o.order_number, :query),
                similarity(COALESCE(o.customer_name, ''), :query),
                similarity(COALESCE(o.status, ''), :query)
            ) as similarity_score
        FROM search.search_orders o
        WHERE o.tenant_id = :tenantId
        AND (
            similarity(o.order_number, :query) >= 0.3 OR
            similarity(COALESCE(o.customer_name, ''), :query) >= 0.3 OR
            similarity(COALESCE(o.status, ''), :query) >= 0.3
        )
        ORDER BY similarity_score DESC, o.order_date DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SearchOrder> fuzzySearch(
        @Param("tenantId") String tenantId,
        @Param("query") String query,
        @Param("limit") int limit
    );
    
    /**
     * Autocomplete suggestions using prefix matching
     */
    @Query(value = """
        SELECT DISTINCT o.*
        FROM search.search_orders o
        WHERE o.tenant_id = :tenantId
        AND o.search_vector @@ to_tsquery('english', :prefixQuery)
        ORDER BY ts_rank(o.search_vector, to_tsquery('english', :prefixQuery)) DESC, o.order_date DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SearchOrder> autocompleteSuggestions(
        @Param("tenantId") String tenantId,
        @Param("prefixQuery") String prefixQuery,
        @Param("limit") int limit
    );
    
    /**
     * Find by entity ID and tenant
     */
    Optional<SearchOrder> findByTenantIdAndEntityId(String tenantId, String entityId);
    
    /**
     * Delete by entity ID and tenant
     */
    void deleteByTenantIdAndEntityId(String tenantId, String entityId);
    
    /**
     * Count by tenant
     */
    long countByTenantId(String tenantId);
}
