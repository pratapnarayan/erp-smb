import React from 'react';

/**
 * Autocomplete dropdown for global search.
 * 
 * Rules:
 * - Max 10 suggestions
 * - Unified rendering using SearchHit DTO only
 * - No entity-specific branching
 * - Icons based on entityType enum only
 */
const SearchAutocomplete = ({ suggestions, selectedIndex, onSelect, onViewAll, error, loading }) => {
  
  // Entity type icons (simple, no branching logic)
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
  
  if (error) {
    return (
      <div className="search-dropdown" style={styles.dropdown}>
        <div style={styles.errorMessage}>
          {error}
        </div>
      </div>
    );
  }
  
  if (loading) {
    return (
      <div className="search-dropdown" style={styles.dropdown}>
        <div style={styles.loadingState}>
          <div style={styles.skeleton}></div>
          <div style={styles.skeleton}></div>
          <div style={styles.skeleton}></div>
        </div>
      </div>
    );
  }
  
  if (suggestions.length === 0) {
    return (
      <div className="search-dropdown" style={styles.dropdown}>
        <div style={styles.emptyState}>
          No matches found
        </div>
      </div>
    );
  }
  
  return (
    <div className="search-dropdown" style={styles.dropdown}>
      <div style={styles.suggestionsList}>
        {suggestions.slice(0, 10).map((suggestion, index) => (
          <div
            key={`${suggestion.entityType}-${suggestion.entityId}`}
            style={{
              ...styles.suggestionItem,
              ...(index === selectedIndex ? styles.suggestionItemSelected : {})
            }}
            onClick={() => onSelect(suggestion)}
            onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f3f4f6'}
            onMouseLeave={(e) => {
              if (index !== selectedIndex) {
                e.currentTarget.style.backgroundColor = 'transparent';
              }
            }}
          >
            <div style={styles.suggestionIcon}>
              <span style={{ fontSize: '20px' }}>
                {getEntityIcon(suggestion.entityType)}
              </span>
            </div>
            <div style={styles.suggestionContent}>
              <div style={styles.suggestionTitle}>
                {suggestion.title}
              </div>
              {suggestion.subtitle && (
                <div style={styles.suggestionSubtitle}>
                  {suggestion.subtitle}
                </div>
              )}
            </div>
            <div 
              style={{
                ...styles.suggestionBadge,
                backgroundColor: getEntityColor(suggestion.entityType) + '20',
                color: getEntityColor(suggestion.entityType)
              }}
            >
              {suggestion.entityType}
            </div>
          </div>
        ))}
      </div>
      
      <div style={styles.dropdownFooter} onClick={onViewAll}>
        <span style={styles.viewAllLink}>
          View all results →
        </span>
      </div>
    </div>
  );
};

const styles = {
  dropdown: {
    position: 'absolute',
    top: '100%',
    left: 0,
    right: 0,
    marginTop: '8px',
    backgroundColor: 'white',
    border: '1px solid #e5e7eb',
    borderRadius: '8px',
    boxShadow: '0 10px 25px rgba(0, 0, 0, 0.1)',
    zIndex: 1000,
    maxHeight: '400px',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column'
  },
  suggestionsList: {
    overflowY: 'auto',
    maxHeight: '350px'
  },
  suggestionItem: {
    display: 'flex',
    alignItems: 'center',
    padding: '12px 16px',
    cursor: 'pointer',
    transition: 'background-color 0.15s',
    gap: '12px'
  },
  suggestionItemSelected: {
    backgroundColor: '#f3f4f6'
  },
  suggestionIcon: {
    flexShrink: 0,
    width: '32px',
    height: '32px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center'
  },
  suggestionContent: {
    flex: 1,
    minWidth: 0
  },
  suggestionTitle: {
    fontSize: '14px',
    fontWeight: '500',
    color: '#111827',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap'
  },
  suggestionSubtitle: {
    fontSize: '12px',
    color: '#6b7280',
    marginTop: '2px',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap'
  },
  suggestionBadge: {
    fontSize: '10px',
    fontWeight: '600',
    padding: '4px 8px',
    borderRadius: '4px',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
    flexShrink: 0
  },
  dropdownFooter: {
    padding: '12px 16px',
    borderTop: '1px solid #e5e7eb',
    backgroundColor: '#f9fafb',
    cursor: 'pointer',
    textAlign: 'center'
  },
  viewAllLink: {
    fontSize: '13px',
    color: '#3b82f6',
    fontWeight: '500'
  },
  emptyState: {
    padding: '24px',
    textAlign: 'center',
    color: '#6b7280',
    fontSize: '14px'
  },
  errorMessage: {
    padding: '16px',
    textAlign: 'center',
    color: '#ef4444',
    fontSize: '14px',
    fontWeight: '500'
  },
  loadingState: {
    padding: '12px 16px'
  },
  skeleton: {
    height: '48px',
    backgroundColor: '#f3f4f6',
    borderRadius: '4px',
    marginBottom: '8px',
    animation: 'pulse 1.5s ease-in-out infinite'
  }
};

export default SearchAutocomplete;
