# Search Service API Documentation

## Overview

The Search Service provides tenant-aware, performant global search across Products, Customers, and Orders using PostgreSQL full-text search.

## Recent Updates (2026)

- **Exact code/identifier search**: Code-like queries such as `SO-1005` or `SKU-1005` are treated as exact matches against identifier fields to avoid noisy results from tokenization and fuzzy matching.
- **Frontend UX**: After submitting a query and navigating to the search results page, the global search input resets (clears query + suggestions).
- **Security**: Indexing and bulk reindex endpoints require `ADMIN` role (internal service-to-service).

**Base URL:** `http://localhost:8090` (direct) or `http://localhost:8080/api/search` (via gateway)

**Version:** 1.0 (MVP)

---

## Supported Entity Types (V1)

| Entity Type | Description | Searchable Fields |
|------------|-------------|-------------------|
| `PRODUCT` | Products/Items | name, sku, category, description |
| `CUSTOMER` | Customers | customer_name, email, phone, gst_number, address |
| `ORDER` | Sales Orders | order_number, customer_name, status |

**Not supported in V1:** ENQUIRY, EMPLOYEE, INVOICE

---

## User-Facing Endpoints

### 1. Global Search

**Endpoint:** `GET /api/search`

**Description:** Search across all entity types or filter by specific type.

**Authentication:** Required (JWT Bearer token)

**Rate Limit:** 20 requests per minute (moderate)

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `q` | string | Yes | - | Search query |
| `type` | enum | No | null | Filter by entity type (PRODUCT, CUSTOMER, ORDER) |
| `fuzzy` | boolean | No | true | Enable fuzzy matching |
| `page` | integer | No | 0 | Page number (0-based) |
| `size` | integer | No | 20 | Results per page (max: 100) |

**Headers:**
- `Authorization: Bearer <jwt-token>` (required)
- `X-Tenant-Id: <tenant-id>` (required)

**Response:**
```json
{
  "results": [
    {
      "entityType": "PRODUCT",
      "entityId": "123",
      "title": "Laptop Computer",
      "subtitle": "SKU: LAP-001",
      "highlight": "Laptop Computer",
      "relevanceScore": 0.95,
      "rawData": null
    }
  ],
  "totalCount": 1,
  "page": 0,
  "size": 20,
  "searchTimeMs": 45,
  "query": "laptop"
}
```

**Performance Target:** p95 latency < 300ms

**Example:**
```bash
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-Id: demo" \
     "http://localhost:8080/api/search?q=laptop&type=PRODUCT&size=10"
```

---

### 2. Autocomplete Suggestions

**Endpoint:** `GET /api/search/suggestions`

**Description:** Get autocomplete suggestions for typeahead/dropdown.

**Authentication:** Required (JWT Bearer token)

**Rate Limit:** 60 requests per minute (strict - designed for keystroke traffic)

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `q` | string | Yes | - | Search query (min 2 characters) |
| `type` | enum | No | null | Filter by entity type |

**Headers:**
- `Authorization: Bearer <jwt-token>` (required)
- `X-Tenant-Id: <tenant-id>` (required)

**Response:**
```json
[
  {
    "text": "Laptop Computer",
    "entityType": "PRODUCT",
    "entityId": "123",
    "highlight": "Laptop Computer",
    "relevanceScore": 0.95
  }
]
```

**Performance Target:** p95 latency < 150ms

**Example:**
```bash
curl -H "Authorization: Bearer <token>" \
     -H "X-Tenant-Id: demo" \
     "http://localhost:8080/api/search/suggestions?q=lap"
```

---

## Internal/System Endpoints

### 3. Index Entity

**Endpoint:** `POST /api/search/index/{entityType}`

**Authentication:** ADMIN role only (internal service-to-service)

**Rate Limit:** None

**Description:** Add or update entity in search index.

**Available Endpoints:**
- `POST /api/search/index/products`
- `POST /api/search/index/customers`
- `POST /api/search/index/orders`

**Request Body:**
```json
{
  "entityId": "123",
  "tenantId": "demo",
  "entityType": "PRODUCT",
  "searchableFields": {
    "name": "Laptop Computer",
    "sku": "LAP-001",
    "category": "Electronics",
    "description": "High-performance laptop"
  }
}
```

**Response:** `200 OK` (empty body)

**Important:** 
- ⚠️ NOT accessible via gateway
- ⚠️ Called by entity services only
- ⚠️ Uses internal ADMIN token

---

### 4. Delete from Index

**Endpoint:** `DELETE /api/search/index/{entityType}/{entityId}`

**Authentication:** ADMIN role only

**Description:** Remove entity from search index.

**Example:** `DELETE /api/search/index/PRODUCT/123`

**Headers:**
- `Authorization: Bearer <admin-token>`
- `X-Tenant-Id: <tenant-id>`

---

### 5. Bulk Reindex

**Endpoint:** `POST /api/search/reindex/{entityType}`

**Authentication:** ADMIN role only

**Description:** Rebuild search index for entire entity type. Idempotent.

**Use Cases:**
- Initial production rollout
- Recovery from index corruption
- Environment bootstrap

**Example:** `POST /api/search/reindex/PRODUCT`

**Response:**
```json
{
  "entityType": "PRODUCT",
  "tenantId": "demo",
  "reindexedCount": 1500,
  "durationMs": 2350,
  "status": "completed"
}
```

---

### 6. Bulk Reindex All

**Endpoint:** `POST /api/search/reindex/all`

**Authentication:** ADMIN role only

**Description:** Rebuild search index for all v1 entity types.

**Response:**
```json
{
  "tenantId": "demo",
  "entityTypes": ["PRODUCT", "CUSTOMER", "ORDER"],
  "totalReindexedCount": 3500,
  "durationMs": 5250,
  "status": "completed"
}
```

---

## Rate Limits

| Endpoint | Limit | Window | Scope |
|----------|-------|--------|-------|
| `/api/search` | 20 requests | 1 minute | Per tenant (or IP fallback) |
| `/api/search/suggestions` | 60 requests | 1 minute | Per tenant (or IP fallback) |
| Indexing endpoints | None | - | Internal only |

**Rate Limit Response:**
```
HTTP 429 Too Many Requests
X-RateLimit-Limit: 20
X-RateLimit-Remaining: 0
Retry-After: 60

{
  "error": "Rate limit exceeded",
  "retryAfter": 60
}
```

---

## Security

### Authentication
- **JWT Bearer Token:** Required for all user-facing endpoints
- **ADMIN Token:** Required for indexing/reindex endpoints

### Authorization

**User-Facing Endpoints:**
- Roles allowed: `USER`, `MANAGER`, `OWNER`, `ADMIN`
- Anonymous access: Denied

**Internal Endpoints:**
- Role required: `ADMIN` only
- User roles: Explicitly denied
- Gateway: Does not route these endpoints

### Tenant Isolation
- All queries enforce `tenant_id` filtering at database level
- No cross-tenant data leakage possible
- Tenant ID extracted from `X-Tenant-Id` header

---

## Search Behavior

### Full-Text Search
- Uses PostgreSQL `tsvector` and `plainto_tsquery()`
- Weighted ranking:
  - Primary fields (name, order_number): Weight A
  - Secondary fields (sku, email): Weight B
  - Tertiary fields (category, status): Weight C
  - Description/address: Weight D
- Results sorted by `ts_rank()` descending

### Fuzzy Search
- Uses `pg_trgm` extension for similarity matching
- **Guardrails:**
  - Disabled for queries < 3 characters
  - Similarity threshold: ≥ 0.3
  - Executes only if full-text results insufficient
- Prevents expensive trigram scans

### Autocomplete
- Uses prefix matching with `:*` operator
- Minimum query length: 2 characters
- Returns max 10 suggestions
- Target latency: < 150ms

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Invalid query parameter"
}
```

### 401 Unauthorized
```json
{
  "error": "Authentication required"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden"
}
```
- Returned for indexing endpoints accessed via gateway
- Returned for ADMIN-only endpoints without ADMIN role

### 429 Too Many Requests
```json
{
  "error": "Rate limit exceeded",
  "retryAfter": 60
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal server error"
}
```

---

## Known Limitations (V1)

1. **Entity Scope:** Only PRODUCT, CUSTOMER, ORDER supported
2. **No Saved Searches:** Not implemented in v1
3. **No Advanced Filters:** Field-specific filtering not available
4. **No Highlighting:** Server-side highlighting not implemented
5. **No Analytics:** Search analytics table exists but not exposed
6. **Synchronous Indexing:** Entity services call search-service synchronously (async planned for v2)

---

## How to Reindex

### Scenario 1: Initial Setup
After deploying search-service for the first time:

1. Ensure search-service is running
2. Use ADMIN token to call reindex endpoints
3. Run for each tenant:
   ```bash
   curl -X POST \
     -H "Authorization: Bearer <admin-token>" \
     -H "X-Tenant-Id: <tenant-id>" \
     http://localhost:8090/api/search/reindex/all
   ```

### Scenario 2: Data Corruption
If search index becomes inconsistent:

1. Reindex specific entity type:
   ```bash
   curl -X POST \
     -H "Authorization: Bearer <admin-token>" \
     -H "X-Tenant-Id: <tenant-id>" \
     http://localhost:8090/api/search/reindex/PRODUCT
   ```

### Scenario 3: Manual Index Update
Normally automatic, but if needed:

```bash
curl -X POST \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "entityId": "123",
    "tenantId": "demo",
    "entityType": "PRODUCT",
    "searchableFields": {
      "name": "Laptop Computer",
      "sku": "LAP-001"
    }
  }' \
  http://localhost:8090/api/search/index/products
```

---

## Configuration

### Application Properties

```yaml
app:
  search:
    max-results: 100          # Maximum results per search
    default-limit: 20         # Default page size
    suggestion-limit: 10      # Max autocomplete suggestions
    min-query-length: 2       # Minimum query length
  jwt:
    secret: <your-secret>
    access-ttl: 3600
```

### Database Configuration

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_schema: search
  flyway:
    enabled: true
    schemas: search
```

---

## Monitoring

### Health Check
```bash
curl http://localhost:8090/actuator/health
```

### Metrics
```bash
curl http://localhost:8090/actuator/metrics
```

### Database Statistics
Query the `search.search_stats` table for analytics:
```sql
SELECT query, COUNT(*) as search_count, AVG(search_time_ms) as avg_time
FROM search.search_stats
WHERE tenant_id = 'demo'
GROUP BY query
ORDER BY search_count DESC
LIMIT 10;
```

---

## Troubleshooting

### Issue: No results found
**Check:**
1. Are entities indexed? Query `search.search_products` table
2. Is tenant_id correct?
3. Try with simpler query (single word)

### Issue: Slow performance
**Check:**
1. Are GIN indexes created? `\di search.*` in psql
2. Is pg_trgm extension enabled? `\dx` in psql
3. Query complexity (avoid very short queries with fuzzy=true)

### Issue: Rate limit too strict
**Solution:**
- Adjust in gateway `RateLimitFilter.java`
- Restart gateway-service

### Issue: Cross-tenant data visible
**Action:**
- **STOP immediately**
- This is a critical security issue
- Check tenant_id filtering in repository queries

---

## Support

- **Service Port:** 8090 (direct), 8080/api/search (gateway)
- **Swagger UI:** http://localhost:8090/swagger-ui.html
- **Database:** PostgreSQL schema `search`
