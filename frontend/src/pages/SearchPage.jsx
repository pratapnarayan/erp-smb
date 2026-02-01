import React, { useState, useEffect } from 'react';
import { globalSearch } from '../api/clients/search';
import SearchResults from '../components/SearchResults';

/**
 * Dedicated search results page.
 * 
 * Route: /search?q=query
 * 
 * Features:
 * - Paginated results
 * - Entity type filters
 * - Relevance-based sorting (server-side only)
 * - Loading and empty states
 * 
 * Rules:
 * - No client-side sorting
 * - No entity-specific fetching
 * - SearchHit DTO only
 */
const SearchPage = ({ query = '', onNavigate }) => {
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [entityTypeFilter, setEntityTypeFilter] = useState(null);

  const pageSize = 20;

  useEffect(() => {
    if (!query.trim()) {
      if (typeof onNavigate === 'function') onNavigate('dashboard');
      return;
    }

    performSearch();
  }, [query, page, entityTypeFilter]);

  const performSearch = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await globalSearch(query, entityTypeFilter, page, pageSize);
      setResults(response);
    } catch (err) {
      if (err.response?.status === 429) {
        setError('Too many searches, please slow down');
      } else {
        setError('Search failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleEntityTypeToggle = (type) => {
    setEntityTypeFilter(prev => prev === type ? null : type);
    setPage(0); // Reset to first page
  };

  const handlePageChange = (newPage) => {
    setPage(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const entityTypes = ['PRODUCT', 'CUSTOMER', 'ORDER'];

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h2 style={styles.title}>
          Search results for "{query}"
        </h2>
        {results && (
          <p style={styles.subtitle}>
            {results.totalCount} results found
            {results.searchTimeMs && ` in ${results.searchTimeMs}ms`}
          </p>
        )}
      </div>

      <div style={styles.filters}>
        <span style={styles.filterLabel}>Filter by:</span>
        {entityTypes.map(type => (
          <button
            key={type}
            onClick={() => handleEntityTypeToggle(type)}
            style={{
              ...styles.filterButton,
              ...(entityTypeFilter === type ? styles.filterButtonActive : {})
            }}
          >
            {type}
          </button>
        ))}
        {entityTypeFilter && (
          <button
            onClick={() => setEntityTypeFilter(null)}
            style={styles.clearFilter}
          >
            Clear filter
          </button>
        )}
      </div>

      {loading && (
        <div style={styles.loadingContainer}>
          <div style={styles.skeletonCard}></div>
          <div style={styles.skeletonCard}></div>
          <div style={styles.skeletonCard}></div>
        </div>
      )}

      {error && (
        <div style={styles.errorContainer}>
          <p style={styles.errorMessage}>{error}</p>
          <button onClick={performSearch} style={styles.retryButton}>
            Retry
          </button>
        </div>
      )}

      {!loading && !error && results && (
        <>
          {results.totalCount === 0 ? (
            <div style={styles.emptyState}>
              <p style={styles.emptyMessage}>No results found for "{query}"</p>
              <p style={styles.emptyHint}>Try different keywords or check your spelling</p>
            </div>
          ) : (
            <>
              <SearchResults results={results.results} onNavigate={onNavigate} />

              {results.totalCount > pageSize && (
                <div style={styles.pagination}>
                  <button
                    onClick={() => handlePageChange(page - 1)}
                    disabled={page === 0}
                    style={{
                      ...styles.paginationButton,
                      ...(page === 0 ? styles.paginationButtonDisabled : {})
                    }}
                  >
                    Previous
                  </button>

                  <span style={styles.paginationInfo}>
                    Page {page + 1} of {Math.ceil(results.totalCount / pageSize)}
                  </span>

                  <button
                    onClick={() => handlePageChange(page + 1)}
                    disabled={!results.hasNext}
                    style={{
                      ...styles.paginationButton,
                      ...(!results.hasNext ? styles.paginationButtonDisabled : {})
                    }}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
};

const styles = {
  container: {
    maxWidth: '900px',
    margin: '0 auto',
    padding: '24px'
  },
  header: {
    marginBottom: '24px'
  },
  title: {
    fontSize: '24px',
    fontWeight: '600',
    color: '#111827',
    marginBottom: '8px'
  },
  subtitle: {
    fontSize: '14px',
    color: '#6b7280'
  },
  filters: {
    display: 'flex',
    alignItems: 'center',
    gap: '8px',
    marginBottom: '24px',
    flexWrap: 'wrap'
  },
  filterLabel: {
    fontSize: '14px',
    color: '#6b7280',
    fontWeight: '500'
  },
  filterButton: {
    padding: '8px 16px',
    fontSize: '13px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    backgroundColor: 'white',
    color: '#374151',
    cursor: 'pointer',
    transition: 'all 0.2s'
  },
  filterButtonActive: {
    backgroundColor: '#3b82f6',
    color: 'white',
    borderColor: '#3b82f6'
  },
  clearFilter: {
    padding: '8px 16px',
    fontSize: '13px',
    border: 'none',
    borderRadius: '6px',
    backgroundColor: '#f3f4f6',
    color: '#6b7280',
    cursor: 'pointer'
  },
  loadingContainer: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  skeletonCard: {
    height: '100px',
    backgroundColor: '#f3f4f6',
    borderRadius: '8px',
    animation: 'pulse 1.5s ease-in-out infinite'
  },
  errorContainer: {
    textAlign: 'center',
    padding: '48px 24px'
  },
  errorMessage: {
    fontSize: '16px',
    color: '#ef4444',
    marginBottom: '16px'
  },
  retryButton: {
    padding: '10px 20px',
    fontSize: '14px',
    backgroundColor: '#3b82f6',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer'
  },
  emptyState: {
    textAlign: 'center',
    padding: '64px 24px'
  },
  emptyMessage: {
    fontSize: '18px',
    color: '#111827',
    fontWeight: '500',
    marginBottom: '8px'
  },
  emptyHint: {
    fontSize: '14px',
    color: '#6b7280'
  },
  pagination: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: '16px',
    marginTop: '32px'
  },
  paginationButton: {
    padding: '10px 20px',
    fontSize: '14px',
    border: '1px solid #d1d5db',
    borderRadius: '6px',
    backgroundColor: 'white',
    color: '#374151',
    cursor: 'pointer',
    transition: 'all 0.2s'
  },
  paginationButtonDisabled: {
    opacity: 0.5,
    cursor: 'not-allowed'
  },
  paginationInfo: {
    fontSize: '14px',
    color: '#6b7280'
  }
};

export default SearchPage;
