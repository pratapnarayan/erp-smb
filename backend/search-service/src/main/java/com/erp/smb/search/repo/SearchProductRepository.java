package com.erp.smb.search.repo;

import com.erp.smb.search.domain.SearchProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchProductRepository extends JpaRepository<SearchProduct, Long> {

    /**
     * Full-text search using PostgreSQL tsvector
     */
    @Query(value = """
            SELECT p.*
            FROM search.search_products p
            WHERE p.tenant_id = :tenantId
            AND p.search_vector @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank(p.search_vector, plainto_tsquery('english', :query)) DESC, p.name
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchProduct> fullTextSearch(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            @Param("limit") int limit);

    /**
     * Fuzzy search using pg_trgm similarity with threshold >= 0.3
     */
    @Query(value = """
            SELECT p.*
            FROM search.search_products p
            WHERE p.tenant_id = :tenantId
            AND (
                similarity(CAST(p.name AS text), CAST(:query AS text)) >= 0.3 OR
                similarity(CAST(COALESCE(p.sku, '') AS text), CAST(:query AS text)) >= 0.3 OR
                similarity(CAST(COALESCE(p.category, '') AS text), CAST(:query AS text)) >= 0.3
            )
            ORDER BY
                GREATEST(
                    similarity(CAST(p.name AS text), CAST(:query AS text)),
                    similarity(CAST(COALESCE(p.sku, '') AS text), CAST(:query AS text)),
                    similarity(CAST(COALESCE(p.category, '') AS text), CAST(:query AS text))
                ) DESC,
                p.name
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchProduct> fuzzySearch(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            @Param("limit") int limit);

    /**
     * Autocomplete suggestions using prefix matching
     */
    @Query(value = """
            SELECT p.*
            FROM search.search_products p
            WHERE p.tenant_id = :tenantId
            AND p.search_vector @@ to_tsquery('english', :prefixQuery)
            ORDER BY ts_rank(p.search_vector, to_tsquery('english', :prefixQuery)) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchProduct> autocompleteSuggestions(
            @Param("tenantId") String tenantId,
            @Param("prefixQuery") String prefixQuery,
            @Param("limit") int limit);

    /**
     * Exact code match (SKU or entity_id).
     *
     * Used for code-like queries (e.g., SKU-1005) to avoid noisy full-text/fuzzy results.
     */
    @Query(value = """
            SELECT p.*
            FROM search.search_products p
            WHERE p.tenant_id = :tenantId
              AND (
                lower(p.sku) = lower(:code) OR
                lower(p.entity_id) = lower(:code)
              )
            ORDER BY p.name
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchProduct> exactCodeMatch(
            @Param("tenantId") String tenantId,
            @Param("code") String code,
            @Param("limit") int limit);

    /**
     * Find by entity ID and tenant
     */
    Optional<SearchProduct> findByTenantIdAndEntityId(String tenantId, String entityId);

    /**
     * Delete by entity ID and tenant
     */
    void deleteByTenantIdAndEntityId(String tenantId, String entityId);

    /**
     * Count by tenant
     */
    long countByTenantId(String tenantId);
}
