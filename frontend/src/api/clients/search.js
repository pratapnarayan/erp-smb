import http from './http';

/**
 * Search API client.
 * Single source of truth for all search operations.
 * 
 * Rules:
 * - Only calls /api/search and /api/search/suggestions
 * - No direct entity service calls
 * - Returns SearchHit DTOs only
 */

/**
 * Global search across all entities
 * @param {string} query - Search query
 * @param {string} entityType - Optional entity type filter (PRODUCT, CUSTOMER, ORDER)
 * @param {number} page - Page number (0-based)
 * @param {number} size - Page size
 * @returns {Promise<SearchResult>} Search results with SearchHit[]
 */
export const globalSearch = async (query, entityType = null, page = 0, size = 20) => {
  const params = {
    q: query,
    page,
    size
  };
  
  if (entityType) {
    params.type = entityType;
  }
  
  const response = await http.get('/search', { params });
  return response.data;
};

/**
 * Get autocomplete suggestions
 * @param {string} query - Search query (min 2 chars)
 * @param {string} entityType - Optional entity type filter
 * @returns {Promise<SearchSuggestion[]>} Autocomplete suggestions
 */
export const getSearchSuggestions = async (query, entityType = null) => {
  if (!query || query.trim().length < 2) {
    return [];
  }
  
  const params = { q: query.trim() };
  
  if (entityType) {
    params.type = entityType;
  }
  
  const response = await http.get('/search/suggestions', { params });
  return response.data;
};
