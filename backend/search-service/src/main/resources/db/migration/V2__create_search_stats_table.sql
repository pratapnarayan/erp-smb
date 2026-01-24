-- Create search statistics table for analytics and monitoring
CREATE TABLE search.search_stats (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    query TEXT NOT NULL,
    entity_type VARCHAR(50),
    results_count INTEGER,
    search_time_ms BIGINT,
    user_id VARCHAR(255),
    searched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for search stats
CREATE INDEX idx_search_stats_tenant ON search.search_stats(tenant_id);
CREATE INDEX idx_search_stats_query ON search.search_stats USING gin(query gin_trgm_ops);
CREATE INDEX idx_search_stats_searched_at ON search.search_stats(searched_at DESC);
CREATE INDEX idx_search_stats_composite ON search.search_stats(tenant_id, searched_at DESC);

-- Create view for popular searches
CREATE OR REPLACE VIEW search.popular_searches AS
SELECT 
    tenant_id,
    query,
    entity_type,
    COUNT(*) as search_count,
    AVG(search_time_ms) as avg_search_time_ms,
    MAX(searched_at) as last_searched_at
FROM search.search_stats
WHERE searched_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY tenant_id, query, entity_type
ORDER BY search_count DESC;
