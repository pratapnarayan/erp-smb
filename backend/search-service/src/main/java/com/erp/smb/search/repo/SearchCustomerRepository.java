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
            SELECT c.*
            FROM search.search_customers c
            WHERE c.tenant_id = :tenantId
            AND c.search_vector @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank(c.search_vector, plainto_tsquery('english', :query)) DESC, c.customer_name
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchCustomer> fullTextSearch(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            @Param("limit") int limit);

    /**
     * Fuzzy search using pg_trgm similarity with threshold >= 0.3
     */
    @Query(value = """
            SELECT c.*
            FROM search.search_customers c
            WHERE c.tenant_id = :tenantId
            AND (
                similarity(CAST(c.customer_name AS text), CAST(:query AS text)) >= 0.3 OR
                similarity(CAST(COALESCE(c.email, '') AS text), CAST(:query AS text)) >= 0.3 OR
                similarity(CAST(COALESCE(c.phone, '') AS text), CAST(:query AS text)) >= 0.3
            )
            ORDER BY
                GREATEST(
                    similarity(CAST(c.customer_name AS text), CAST(:query AS text)),
                    similarity(CAST(COALESCE(c.email, '') AS text), CAST(:query AS text)),
                    similarity(CAST(COALESCE(c.phone, '') AS text), CAST(:query AS text))
                ) DESC,
                c.customer_name
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchCustomer> fuzzySearch(
            @Param("tenantId") String tenantId,
            @Param("query") String query,
            @Param("limit") int limit);

    /**
     * Autocomplete suggestions using prefix matching
     */
    @Query(value = """
            SELECT c.*
            FROM search.search_customers c
            WHERE c.tenant_id = :tenantId
            AND c.search_vector @@ to_tsquery('english', :prefixQuery)
            ORDER BY ts_rank(c.search_vector, to_tsquery('english', :prefixQuery)) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchCustomer> autocompleteSuggestions(
            @Param("tenantId") String tenantId,
            @Param("prefixQuery") String prefixQuery,
            @Param("limit") int limit);

    /**
     * Exact code match (entity_id, email, phone, or GST).
     *
     * Used for code-like queries (e.g., GSTIN/phone/email fragments) to avoid noisy full-text/fuzzy results.
     */
    @Query(value = """
            SELECT c.*
            FROM search.search_customers c
            WHERE c.tenant_id = :tenantId
              AND (
                lower(c.entity_id) = lower(:code) OR
                lower(COALESCE(c.email, '')) = lower(:code) OR
                lower(COALESCE(c.phone, '')) = lower(:code) OR
                lower(COALESCE(c.gst_number, '')) = lower(:code)
              )
            ORDER BY c.customer_name
            LIMIT :limit
            """, nativeQuery = true)
    List<SearchCustomer> exactCodeMatch(
            @Param("tenantId") String tenantId,
            @Param("code") String code,
            @Param("limit") int limit);

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
