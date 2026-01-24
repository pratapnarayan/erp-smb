import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import SearchAutocomplete from './SearchAutocomplete';
import { getSearchSuggestions } from '../api/clients/search';

/**
 * Global search bar with autocomplete.
 * 
 * Features:
 * - 300ms debounce
 * - Triggers autocomplete after 2 characters
 * - Keyboard navigation (↑↓ Enter Esc)
 * - Cancels in-flight requests
 * - Handles rate limiting (429)
 */
const GlobalSearch = () => {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showDropdown, setShowDropdown] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const [error, setError] = useState(null);
  
  const inputRef = useRef(null);
  const dropdownRef = useRef(null);
  const debounceTimer = useRef(null);
  const abortController = useRef(null);
  
  const navigate = useNavigate();
  
  // Debounced autocomplete
  useEffect(() => {
    // Clear previous timer
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }
    
    // Cancel previous request
    if (abortController.current) {
      abortController.current.abort();
    }
    
    // Reset state
    setError(null);
    setSelectedIndex(-1);
    
    if (query.trim().length < 2) {
      setSuggestions([]);
      setShowDropdown(false);
      return;
    }
    
    // Debounce 300ms
    debounceTimer.current = setTimeout(async () => {
      setLoading(true);
      
      try {
        // Create new abort controller
        abortController.current = new AbortController();
        
        const results = await getSearchSuggestions(query);
        setSuggestions(results || []);
        setShowDropdown(true);
        setError(null);
      } catch (err) {
        if (err.name === 'AbortError' || err.code === 'ERR_CANCELED') {
          // Request cancelled, ignore
          return;
        }
        
        if (err.response?.status === 429) {
          setError('Too many searches, please slow down');
        } else {
          setError('Search failed');
        }
        setSuggestions([]);
      } finally {
        setLoading(false);
      }
    }, 300);
    
    return () => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
    };
  }, [query]);
  
  // Keyboard navigation
  const handleKeyDown = (e) => {
    if (!showDropdown || suggestions.length === 0) {
      if (e.key === 'Enter' && query.trim()) {
        // Full search on Enter
        handleSearch();
      }
      return;
    }
    
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setSelectedIndex(prev => 
          prev < suggestions.length - 1 ? prev + 1 : 0
        );
        break;
      
      case 'ArrowUp':
        e.preventDefault();
        setSelectedIndex(prev => 
          prev > 0 ? prev - 1 : suggestions.length - 1
        );
        break;
      
      case 'Enter':
        e.preventDefault();
        if (selectedIndex >= 0 && selectedIndex < suggestions.length) {
          handleSelectSuggestion(suggestions[selectedIndex]);
        } else {
          handleSearch();
        }
        break;
      
      case 'Escape':
        e.preventDefault();
        setShowDropdown(false);
        setSelectedIndex(-1);
        break;
      
      default:
        break;
    }
  };
  
  const handleSelectSuggestion = (suggestion) => {
    setShowDropdown(false);
    setQuery('');
    setSuggestions([]);
    
    // Navigate to entity detail page
    const entityRoutes = {
      PRODUCT: '/inventory',
      CUSTOMER: '/sales',
      ORDER: '/orders'
    };
    
    const route = entityRoutes[suggestion.entityType] || '/';
    navigate(route);
  };
  
  const handleSearch = () => {
    if (query.trim()) {
      setShowDropdown(false);
      navigate(`/search?q=${encodeURIComponent(query.trim())}`);
    }
  };
  
  const handleClickOutside = (e) => {
    if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
      setShowDropdown(false);
    }
  };
  
  useEffect(() => {
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);
  
  return (
    <div className="global-search" ref={dropdownRef} style={{ position: 'relative', width: '100%', maxWidth: '500px' }}>
      <div style={{ position: 'relative' }}>
        <input
          ref={inputRef}
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          onFocus={() => query.trim().length >= 2 && setShowDropdown(true)}
          placeholder="Search products, customers, orders..."
          style={{
            width: '100%',
            padding: '10px 40px 10px 15px',
            fontSize: '14px',
            border: '1px solid #ddd',
            borderRadius: '8px',
            outline: 'none',
            backgroundColor: 'rgba(255, 255, 255, 0.9)'
          }}
        />
        
        {loading && (
          <div style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)' }}>
            <div className="spinner-small"></div>
          </div>
        )}
      </div>
      
      {showDropdown && (
        <SearchAutocomplete
          suggestions={suggestions}
          selectedIndex={selectedIndex}
          onSelect={handleSelectSuggestion}
          onViewAll={handleSearch}
          error={error}
          loading={loading}
        />
      )}
    </div>
  );
};

export default GlobalSearch;
