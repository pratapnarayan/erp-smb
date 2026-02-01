import React, { useEffect, useMemo, useState } from 'react';
import AppShell from './components/AppShell.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Sales from './pages/Sales.jsx';
import Inventory from './pages/Inventory.jsx';
import Settings from './pages/Settings.jsx';
import Enquiry from './pages/Enquiry.jsx';
import Orders from './pages/Orders.jsx';
import Finance from './pages/Finance.jsx';
import HRMS from './pages/HRMS.jsx';
import Admin from './pages/Admin.jsx';
import Login from './pages/Login.jsx';
import Reporting from './pages/Reporting.jsx';
import SearchPage from './pages/SearchPage.jsx';

const routes = [
  { key: 'dashboard', label: 'Dashboard', icon: '📊' },
  { key: 'enquiry', label: 'Enquiry', icon: '❓' },
  { key: 'orders', label: 'Orders', icon: '🧾' },
  { key: 'sales', label: 'Sales', icon: '💸' },
  { key: 'inventory', label: 'Inventory', icon: '📦' },
  { key: 'finance', label: 'Finance', icon: '💼' },
  { key: 'hrms', label: 'HRMS', icon: '👥' },
  { key: 'reporting', label: 'Reporting', icon: '📈' },
  { key: 'admin', label: 'Admin', icon: '🛡️' },
  { key: 'settings', label: 'Settings', icon: '⚙️' },
];

export default function App() {
  const [route, setRoute] = useState('dashboard');
  const [searchQuery, setSearchQuery] = useState('');
  const [theme, setTheme] = useState(() => localStorage.getItem('theme') || 'light');
  const [user, setUser] = useState(() => {
    try {
      const u = JSON.parse(localStorage.getItem('user')) || null;
      const at = localStorage.getItem('accessToken');
      return at ? u : null;
    } catch { return null; }
  });

  useEffect(() => {
    const onLogout = () => {
      setUser(null);
      localStorage.removeItem('user');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    };
    window.addEventListener('auth:logout', onLogout);
    return () => window.removeEventListener('auth:logout', onLogout);
  }, []);

  const Page = useMemo(() => {
    switch (route) {
      case 'dashboard':
        return <Dashboard />;
      case 'enquiry':
        return <Enquiry />;
      case 'orders':
        return <Orders />;
      case 'sales':
        return <Sales />;
      case 'inventory':
        return <Inventory />;
      case 'finance':
        return <Finance />;
      case 'hrms':
        return <HRMS />;
      case 'admin':
        return <Admin />;
      case 'reporting':
        return <Reporting />;
      case 'settings':
        return <Settings theme={theme} setTheme={setTheme} />;
      case 'search':
        return <SearchPage query={searchQuery} onNavigate={setRoute} />;
      default:
        return <Dashboard />;
    }
  }, [route, theme, searchQuery]);

  if (!user) {
    return (
      <div className={`app app--${theme}`}>
        <main className="main">
          <section className="content">
            {<Login onLogin={(u) => { setUser(u); localStorage.setItem('user', JSON.stringify(u)); }} />}
          </section>
        </main>
      </div>
    );
  }

  return (
    <AppShell
      theme={theme}
      setTheme={(t) => {
        setTheme(t);
        localStorage.setItem('theme', t);
      }}
      route={route}
      onNavigate={(keyOrObj) => {
        // Support both simple route changes and search navigation payloads
        if (typeof keyOrObj === 'string') {
          setRoute(keyOrObj);
          return;
        }
        if (keyOrObj && typeof keyOrObj === 'object') {
          if (keyOrObj.type === 'search') {
            setSearchQuery(keyOrObj.query || '');
            setRoute('search');
            return;
          }
          if (keyOrObj.route) {
            setRoute(keyOrObj.route);
          }
        }
      }}
      routes={routes}
      user={user}
      onLogout={() => { setUser(null); localStorage.removeItem('user'); localStorage.removeItem('accessToken'); localStorage.removeItem('refreshToken'); }}
    >
      {React.cloneElement(Page, { currentRole: user.role })}
    </AppShell>
  );
}
