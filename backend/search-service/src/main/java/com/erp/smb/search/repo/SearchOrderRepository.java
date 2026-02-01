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
            SELECT o.*
            FROM search.search_orders o
            WHERE o.tenant_id = :tenantId
            AND o.search_vector @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank(o.search_vector, plainto_tsquery('english', :query)) DESC, o.order_date DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchOrder> fullTextSearch(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            @Param("limit") int limit);

    /**
     * Fuzzy search using pg_trgm similarity with threshold >= 0.3
     */
    @Query(value = """
            SELECT o.*
            FROM search.search_orders o
            WHERE o.tenant_id = :tenantId
            AND (
                similarity(CAST(o.order_number AS text), CAST(:query AS text)) >= 0.3 OR
                similarity(CAST(COALESCE(o.customer_name, '') AS text), CAST(:query AS text)) >= 0.3 OR
                similarity(CAST(COALESCE(o.status, '') AS text), CAST(:query AS text)) >= 0.3
            )
            ORDER BY
                GREATEST(
                    similarity(CAST(o.order_number AS text), CAST(:query AS text)),
                    similarity(CAST(COALESCE(o.customer_name, '') AS text), CAST(:query AS text)),
                    similarity(CAST(COALESCE(o.status, '') AS text), CAST(:query AS text))
                ) DESC,
                o.order_date DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchOrder> fuzzySearch(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            @Param("limit") int limit);

    /**
     * Autocomplete suggestions using prefix matching
     */
    @Query(value = """
            SELECT o.*
            FROM search.search_orders o
            WHERE o.tenant_id = :tenantId
            AND o.search_vector @@ to_tsquery('english', :prefixQuery)
            ORDER BY ts_rank(o.search_vector, to_tsquery('english', :prefixQuery)) DESC, o.order_date DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchOrder> autocompleteSuggestions(
            @Param("tenantId") String tenantId,
            @Param("prefixQuery") String prefixQuery,
            @Param("limit") int limit);

    /**
     * Exact code match (order_number or entity_id).
     *
     * Used for code-like queries (e.g., SO-1005) to avoid noisy full-text/fuzzy results.
     */
    @Query(value = """
            SELECT o.*
            FROM search.search_orders o
            WHERE o.tenant_id = :tenantId
              AND (
                lower(o.order_number) = lower(:code) OR
                lower(o.entity_id) = lower(:code)
              )
            ORDER BY o.order_date DESC NULLS LAST
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchOrder> exactCodeMatch(
            @Param("tenantId") String tenantId,
            @Param("code") String code,
            @Param("limit") int limit);

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
