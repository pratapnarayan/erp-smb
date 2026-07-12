import React, { useEffect, useState } from 'react';
import {
  Routes,
  Route,
  Navigate,
  useNavigate,
  useLocation,
  useSearchParams,
} from 'react-router-dom';

import AppShell    from './components/AppShell.jsx';
import Dashboard   from './pages/Dashboard.jsx';
import Sales       from './pages/Sales.jsx';
import Inventory   from './pages/Inventory.jsx';
import Settings    from './pages/Settings.jsx';
import Enquiry     from './pages/Enquiry.jsx';
import Orders      from './pages/Orders.jsx';
import Finance     from './pages/Finance.jsx';
import HRMS        from './pages/HRMS.jsx';
import Admin       from './pages/Admin.jsx';
import Login       from './pages/Login.jsx';
import Reporting   from './pages/Reporting.jsx';
import SearchPage  from './pages/SearchPage.jsx';
import AuditLog    from './pages/AuditLog.jsx';
import { authApi } from './api/clients/index.js';

/** Sidebar route definitions — key maps to the URL path segment. */
export const routes = [
  { key: 'dashboard', label: 'Dashboard', icon: '📊' },
  { key: 'enquiry',   label: 'Enquiry',   icon: '❓' },
  { key: 'orders',    label: 'Orders',    icon: '🧾' },
  { key: 'sales',     label: 'Sales',     icon: '💸' },
  { key: 'inventory', label: 'Inventory', icon: '📦' },
  { key: 'finance',   label: 'Finance',   icon: '💼' },
  { key: 'hrms',      label: 'HRMS',      icon: '👥' },
  { key: 'reporting', label: 'Reporting', icon: '📈' },
  { key: 'admin',     label: 'Admin',     icon: '🛡️' },
  { key: 'auditlog',  label: 'Audit Log', icon: '📋' },
  { key: 'settings',  label: 'Settings',  icon: '⚙️' },
];

/**
 * App root component.
 *
 * Session management:
 *   - Tokens live in HttpOnly cookies (set/cleared by the server) — JS cannot
 *     read them, so a `user` entry in localStorage alone is not proof of a
 *     valid session (it could be stale or left over on a shared machine).
 *   - localStorage stores only { username, role } for UI personalisation.
 *   - On mount, if a `user` entry exists, its session is verified with a
 *     cookie-based refresh call before the authenticated shell is rendered;
 *     an invalid/expired session clears `user` and falls back to /login.
 *   - Any 401 from the API afterwards triggers the same refresh attempt; on
 *     failure, the auth:logout event fires and the user is redirected to /login.
 */
export default function App() {
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') || 'light');
  const [user,  setUser]  = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('user')) || null;
    } catch {
      return null;
    }
  });
  // While true, hold off rendering the authenticated shell for a stale/forged
  // `user` entry until the underlying cookie session is confirmed valid.
  const [verifyingSession, setVerifyingSession] = useState(() => !!localStorage.getItem('user'));

  // On mount, re-establish that a `user` entry actually corresponds to a live
  // session by attempting a cookie-based refresh — a stale/forged entry with
  // no valid refreshToken cookie fails here instead of rendering the shell.
  useEffect(() => {
    if (!user) {
      setVerifyingSession(false);
      return;
    }
    let cancelled = false;
    authApi.refresh()
      .catch(() => {
        if (!cancelled) {
          setUser(null);
          localStorage.removeItem('user');
        }
      })
      .finally(() => {
        if (!cancelled) setVerifyingSession(false);
      });
    return () => { cancelled = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Listen for logout events fired by the token refresh interceptor on failure.
  useEffect(() => {
    const onLogout = () => {
      setUser(null);
      localStorage.removeItem('user');
    };
    window.addEventListener('auth:logout', onLogout);
    return () => window.removeEventListener('auth:logout', onLogout);
  }, []);

  if (verifyingSession) {
    return (
      <div className={`app app--${theme}`}>
        <main className="main">
          <section className="content" />
        </main>
      </div>
    );
  }

  const handleLogin = (u) => {
    setUser(u);
    // user is already saved in localStorage inside Login.jsx
  };

  const handleLogout = async () => {
    try {
      await authApi.logout(); // server clears HttpOnly cookies
    } catch {
      // Proceed with client-side cleanup even if the server call fails.
    }
    setUser(null);
    localStorage.removeItem('user');
  };

  const handleThemeChange = (t) => {
    setTheme(t);
    localStorage.setItem('theme', t);
  };

  if (!user) {
    return (
      <div className={`app app--${theme}`}>
        <main className="main">
          <section className="content">
            <Login onLogin={handleLogin} />
          </section>
        </main>
      </div>
    );
  }

  return (
    <AuthenticatedApp
      user={user}
      theme={theme}
      onThemeChange={handleThemeChange}
      onLogout={handleLogout}
    />
  );
}

/**
 * The routed shell rendered after successful authentication.
 * Separated so that useNavigate / useLocation hooks work inside a <Routes> context.
 */
function AuthenticatedApp({ user, theme, onThemeChange, onLogout }) {
  const navigate = useNavigate();
  const location = useLocation();

  // Derive the active sidebar key from the current path.
  const activeKey = location.pathname.replace(/^\//, '') || 'dashboard';

  /**
   * onNavigate handler forwarded to AppShell and GlobalSearch.
   * Supports:
   *   - string     → navigate to that route path
   *   - { type: 'search', query: '...' } → navigate to /search?q=...
   *   - { route: '...' } → navigate to that route path
   */
  const handleNavigate = (keyOrObj) => {
    if (typeof keyOrObj === 'string') {
      navigate(`/${keyOrObj}`);
      return;
    }
    if (keyOrObj && typeof keyOrObj === 'object') {
      if (keyOrObj.type === 'search') {
        const q = encodeURIComponent(keyOrObj.query || '');
        navigate(`/search?q=${q}`);
        return;
      }
      if (keyOrObj.route) {
        navigate(`/${keyOrObj.route}`);
      }
    }
  };

  const commonProps = { currentRole: user.role, currentUsername: user.username };

  return (
    <AppShell
      theme={theme}
      setTheme={onThemeChange}
      route={activeKey}
      onNavigate={handleNavigate}
      routes={routes}
      user={user}
      onLogout={onLogout}
    >
      <Routes>
        <Route path="/"           element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard"  element={<Dashboard  {...commonProps} />} />
        <Route path="/enquiry"    element={<Enquiry    {...commonProps} />} />
        <Route path="/orders"     element={<Orders     {...commonProps} />} />
        <Route path="/sales"      element={<Sales      {...commonProps} />} />
        <Route path="/inventory"  element={<Inventory  {...commonProps} />} />
        <Route path="/finance"    element={<Finance    {...commonProps} />} />
        <Route path="/hrms"       element={<HRMS       {...commonProps} />} />
        <Route path="/reporting"  element={<Reporting  {...commonProps} />} />
        <Route path="/admin"      element={<Admin      {...commonProps} />} />
        <Route path="/auditlog"   element={<AuditLog   {...commonProps} />} />
        <Route path="/settings"   element={<Settings   {...commonProps} theme={theme} setTheme={onThemeChange} />} />
        <Route path="/search"     element={<SearchPageWrapper {...commonProps} onNavigate={handleNavigate} />} />
        {/* Catch-all: redirect unknown paths to dashboard */}
        <Route path="*"           element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AppShell>
  );
}

/**
 * Thin wrapper that reads the ?q= search param and forwards it to SearchPage.
 */
function SearchPageWrapper({ onNavigate, ...rest }) {
  const [params] = useSearchParams();
  const query = params.get('q') || '';
  return <SearchPage query={query} onNavigate={onNavigate} {...rest} />;
}
