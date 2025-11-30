import React from 'react';
import FrostedCard from '../components/FrostedCard.jsx';

export default function Settings({ theme, setTheme }) {
  return (
    <div className="grid cols-2">
      <FrostedCard title="Appearance" subtitle="Theme and density">
        <div className="grid cols-2">
          <label style={{ display: 'grid', gap: 6 }}>
            <span>Theme</span>
            <select value={theme} onChange={(e) => setTheme(e.target.value)}>
              <option value="light">Light</option>
              <option value="dark">Dark</option>
            </select>
          </label>
          <label style={{ display: 'grid', gap: 6 }}>
            <span>Density</span>
            <select defaultValue="comfortable">
              <option value="compact">Compact</option>
              <option value="comfortable">Comfortable</option>
            </select>
          </label>
        </div>
      </FrostedCard>
      <FrostedCard title="Company" subtitle="Organization details">
        <div className="grid cols-2">
          <label style={{ display: 'grid', gap: 6 }}>
            <span>Legal Name</span>
            <input placeholder="Your company LLC" />
          </label>
          <label style={{ display: 'grid', gap: 6 }}>
            <span>Tax ID</span>
            <input placeholder="12-3456789" />
          </label>
          <label style={{ display: 'grid', gap: 6 }}>
            <span>Address</span>
            <input placeholder="123 Main St" />
          </label>
          <label style={{ display: 'grid', gap: 6 }}>
            <span>City</span>
            <input placeholder="Metropolis" />
          </label>
        </div>
      </FrostedCard>
    </div>
  );
}
