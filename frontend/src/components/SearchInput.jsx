import React from 'react';

export default function SearchInput({ placeholder }) {
  return (
    <label className="search">
      <span className="search-icon" aria-hidden>🔎</span>
      <input className="search-input" type="search" placeholder={placeholder} />
    </label>
  );
}
