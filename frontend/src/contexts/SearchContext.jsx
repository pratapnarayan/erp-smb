import React, { createContext, useContext, useState } from 'react';

/**
 * Search context for managing global search state.
 * Provides centralized state management for search UI.
 */
const SearchContext = createContext();

export const useSearch = () => {
  const context = useContext(SearchContext);
  if (!context) {
    throw new Error('useSearch must be used within SearchProvider');
  }
  return context;
};

export const SearchProvider = ({ children }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [selectedEntityType, setSelectedEntityType] = useState(null);
  
  const openSearch = () => setIsSearchOpen(true);
  const closeSearch = () => setIsSearchOpen(false);
  
  const clearSearch = () => {
    setSearchQuery('');
    setSelectedEntityType(null);
  };
  
  const value = {
    searchQuery,
    setSearchQuery,
    isSearchOpen,
    openSearch,
    closeSearch,
    selectedEntityType,
    setSelectedEntityType,
    clearSearch
  };
  
  return (
    <SearchContext.Provider value={value}>
      {children}
    </SearchContext.Provider>
  );
};
