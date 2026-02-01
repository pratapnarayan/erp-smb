import React from 'react';
import GlobalSearch from './GlobalSearch.jsx';

export default function AppShell({ children, routes, route, onNavigate, theme, setTheme, user, onLogout }) {
  return (
    <div className={`app app--${theme}`}>
      <aside className="sidebar frosted">
        <div className="brand">
          <div className="logo">🧭</div>
          <div className="brand-text">
            <strong>ERP-SMB</strong>
            <small>for SMBs</small>
          </div>
        </div>
        <nav className="nav">
          {routes.map((r) => (
            <button
              key={r.key}
              className={`nav-item ${route === r.key ? 'active' : ''}`}
              onClick={() => onNavigate(r.key)}
              aria-current={route === r.key ? 'page' : undefined}
            >
              <span className="nav-icon" aria-hidden>{r.icon}</span>
              <span>{r.label}</span>
            </button>
          ))}
        </nav>
        <div className="sidebar-footer">
          <button
            className="theme-toggle"
            onClick={() => setTheme(theme === 'light' ? 'dark' : 'light')}
            aria-label="Toggle theme"
          >
            {theme === 'light' ? '🌙 Dark' : '🔆 Light'}
          </button>
        </div>
      </aside>
      <main className="main">
        <header className="topbar frosted">
          <h1 className="topbar-title">{routes.find((r) => r.key === route)?.label}</h1>
          <div className="topbar-actions">
            <GlobalSearch onNavigate={onNavigate} />
            <span className="badge">{user?.role}</span>
            <button className="topbar-btn" aria-label="Notifications">🔔</button>
            <button className="topbar-btn" onClick={onLogout} aria-label="Logout">⎋</button>
            <button className="topbar-btn avatar" aria-label="Account">
              <img src="https://avatars.githubusercontent.com/u/9919?v=4" alt="Avatar" />
            </button>
          </div>
        </header>
        <section className="content">
          {children}
        </section>
      </main>
    </div>
  );
}
