import React from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * Search results list component.
 * 
 * Rules:
 * - Unified rendering using SearchHit DTO only
 * - No entity-specific branching
 * - Icons/colors based on entityType enum only
 * - Relevance-based order (server-provided)
 */
const SearchResults = ({ results }) => {
  const navigate = useNavigate();
  
  // Entity type icons (enum-based only, no branching)
  const getEntityIcon = (entityType) => {
    const icons = {
      PRODUCT: '📦',
      CUSTOMER: '👤',
      ORDER: '📋'
    };
    return icons[entityType] || '📄';
  };
  
  // Entity type colors (enum-based only)
  const getEntityColor = (entityType) => {
    const colors = {
      PRODUCT: '#3b82f6',
      CUSTOMER: '#10b981',
      ORDER: '#f59e0b'
    };
    return colors[entityType] || '#6b7280';
  };
  
  const handleResultClick = (result) => {
    // Navigate to entity detail page (existing routes)
    const entityRoutes = {
      PRODUCT: '/inventory',
      CUSTOMER: '/sales',
      ORDER: '/orders'
    };
    
    const route = entityRoutes[result.entityType] || '/';
    navigate(route);
  };
  
  if (!results || results.length === 0) {
    return null;
  }
  
  return (
    <div style={styles.container}>
      {results.map((result, index) => (
        <div
          key={`${result.entityType}-${result.entityId}-${index}`}
          style={styles.resultCard}
          onClick={() => handleResultClick(result)}
          onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-2px)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
        >
          <div style={styles.resultIcon}>
            <span style={{ fontSize: '28px' }}>
              {getEntityIcon(result.entityType)}
            </span>
          </div>
          
          <div style={styles.resultContent}>
            <div style={styles.resultHeader}>
              <h3 style={styles.resultTitle}>
                {result.title}
              </h3>
              <span
                style={{
                  ...styles.resultBadge,
                  backgroundColor: getEntityColor(result.entityType) + '20',
                  color: getEntityColor(result.entityType)
                }}
              >
                {result.entityType}
              </span>
            </div>
            
            {result.subtitle && (
              <p style={styles.resultSubtitle}>
                {result.subtitle}
              </p>
            )}
            
            {result.highlight && result.highlight !== result.title && (
              <p style={styles.resultHighlight}>
                {result.highlight}
              </p>
            )}
            
            {result.relevanceScore && (
              <div style={styles.resultMeta}>
                Relevance: {Math.round(result.relevanceScore * 100)}%
              </div>
            )}
          </div>
        </div>
      ))}
    </div>
  );
};

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px'
  },
  resultCard: {
    display: 'flex',
    gap: '16px',
    padding: '20px',
    backgroundColor: 'white',
    border: '1px solid #e5e7eb',
    borderRadius: '8px',
    cursor: 'pointer',
    transition: 'all 0.2s',
    boxShadow: '0 1px 2px rgba(0, 0, 0, 0.05)'
  },
  resultIcon: {
    flexShrink: 0,
    width: '48px',
    height: '48px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f9fafb',
    borderRadius: '8px'
  },
  resultContent: {
    flex: 1,
    minWidth: 0
  },
  resultHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: '12px',
    marginBottom: '8px'
  },
  resultTitle: {
    fontSize: '16px',
    fontWeight: '600',
    color: '#111827',
    margin: 0,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
    flex: 1
  },
  resultBadge: {
    fontSize: '10px',
    fontWeight: '600',
    padding: '4px 10px',
    borderRadius: '4px',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
    flexShrink: 0
  },
  resultSubtitle: {
    fontSize: '14px',
    color: '#6b7280',
    margin: '0 0 8px 0',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap'
  },
  resultHighlight: {
    fontSize: '13px',
    color: '#9ca3af',
    margin: '0 0 8px 0',
    fontStyle: 'italic'
  },
  resultMeta: {
    fontSize: '12px',
    color: '#9ca3af'
  }
};

export default SearchResults;
