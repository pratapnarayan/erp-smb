import React, { useEffect, useRef, useState } from 'react';
import GlobalSearch from './GlobalSearch.jsx';
import http from '../api/clients/http.js';

export default function AppShell({ children, routes, route, onNavigate, theme, setTheme, user, onLogout }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false);
  const [notifications, setNotifications] = useState([
    { id: 'n2', text: 'New enquiry received from Tata Motors', time: '4h ago', unread: true },
    { id: 'n3', text: 'Report "Sales Summary" completed', time: '1d ago', unread: false },
  ]);
  const [notifOpen, setNotifOpen] = useState(false);
  const menuRef = useRef(null);
  const notifRef = useRef(null);

  useEffect(() => {
    // Load low-stock alerts from inventory
    (async () => {
      try {
        const { data } = await http.get('/products', { params: { page: 0, size: 100 } });
        const items = data?.content || data || [];
        const lowStock = items.filter((p) => {
          const qty = Number(p.quantity ?? p.qty ?? p.stock ?? p.stockQuantity ?? 0);
          return qty > 0 && qty <= 5;
        });
        if (lowStock.length > 0) {
          const alerts = lowStock.slice(0, 5).map((p, i) => ({
            id: `low-${p.id || i}`,
            text: `Low stock: ${p.name || p.productName || 'Item'} — ${p.quantity ?? p.qty ?? p.stock} unit(s) remaining`,
            time: 'now',
            unread: true,
          }));
          setNotifications((prev) => [...alerts, ...prev]);
        }
      } catch {
        // Non-critical — keep default notifications
      }
    })();
  }, []);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) setMenuOpen(false);
      if (notifRef.current && !notifRef.current.contains(e.target)) setNotifOpen(false);
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const unreadCount = notifications.filter((n) => n.unread).length;

  return (
    <div className={`app app--${theme}`}>
      {/* Logout confirmation dialog */}
      {showLogoutConfirm && (
        <div className="dialog-overlay" role="dialog" aria-modal="true" aria-labelledby="logout-title">
          <div className="dialog">
            <h3 className="dialog-title" id="logout-title">Sign out</h3>
            <p className="dialog-body">Are you sure you want to sign out of ERP-SMB?</p>
            <div className="dialog-actions">
              <button className="btn" onClick={() => setShowLogoutConfirm(false)}>Cancel</button>
              <button className="btn btn-danger" onClick={() => { setShowLogoutConfirm(false); onLogout(); }}>
                Sign out
              </button>
            </div>
          </div>
        </div>
      )}

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

            {/* Notification bell */}
            <div className="user-menu" ref={notifRef}>
              <button
                className="topbar-btn notif-btn"
                aria-label={`Notifications${unreadCount ? ` (${unreadCount} unread)` : ''}`}
                onClick={() => setNotifOpen((o) => !o)}
              >
                🔔
                {unreadCount > 0 && <span className="notif-badge">{unreadCount}</span>}
              </button>
              {notifOpen && (
                <div className="user-dropdown notif-dropdown">
                  <div className="user-dropdown-header">
                    <strong>Notifications</strong>
                    {unreadCount > 0 && (
                      <button
                        style={{ border: 'none', background: 'none', color: 'hsl(var(--primary-500))', cursor: 'pointer', fontSize: 12, float: 'right' }}
                        onClick={() => setNotifications((n) => n.map((x) => ({ ...x, unread: false })))}
                      >
                        Mark all read
                      </button>
                    )}
                  </div>
                  {notifications.length === 0 && (
                    <div style={{ padding: '16px', color: 'hsl(var(--muted))', fontSize: 13, textAlign: 'center' }}>No notifications</div>
                  )}
                  {notifications.map((n) => (
                    <div key={n.id} className={`notif-item${n.unread ? ' notif-item--unread' : ''}`}>
                      <div className="notif-text">{n.text}</div>
                      <div className="notif-time">{n.time}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* User avatar + dropdown */}
            <div className="user-menu" ref={menuRef}>
              <button
                className="topbar-btn avatar"
                aria-label="User menu"
                aria-expanded={menuOpen}
                onClick={() => setMenuOpen((o) => !o)}
              >
                <img src="https://avatars.githubusercontent.com/u/9919?v=4" alt="Avatar" />
              </button>
              {menuOpen && (
                <div className="user-dropdown">
                  <div className="user-dropdown-header">
                    <div className="user-dropdown-name">{user?.username || 'User'}</div>
                    <div className="user-dropdown-role">{user?.role}</div>
                  </div>
                  <hr className="user-dropdown-divider" />
                  <button className="user-dropdown-item" onClick={() => { setMenuOpen(false); onNavigate('settings'); }}>
                    👤 Profile & Settings
                  </button>
                  <button className="user-dropdown-item" onClick={() => { setMenuOpen(false); onNavigate('settings'); }}>
                    🔑 Change Password
                  </button>
                  <hr className="user-dropdown-divider" />
                  <button
                    className="user-dropdown-item user-dropdown-item--danger"
                    onClick={() => { setMenuOpen(false); setShowLogoutConfirm(true); }}
                  >
                    ⎋ Sign out
                  </button>
                </div>
              )}
            </div>
          </div>
        </header>
        <section className="content">
          {children}
        </section>
      </main>
    </div>
  );
}
