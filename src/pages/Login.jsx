import React, { useState } from 'react';
import logoUrl from '../assets/logo.svg';

const allowed = ['admin', 'sales', 'warehouse', 'finance', 'hr'];
const roleMap = {
  admin: 'ADMIN',
  sales: 'SALES',
  warehouse: 'WAREHOUSE',
  finance: 'FINANCE',
  hr: 'HR',
};

export default function Login({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const submit = (e) => {
    e.preventDefault();
    const u = username.trim().toLowerCase();
    const p = password.trim().toLowerCase();
    if (!allowed.includes(u)) {
      setError('Unknown user. Try admin, sales, warehouse, finance, or hr.');
      return;
    }
    if (u !== p) {
      setError('Invalid credentials. Use same user and password, e.g., admin/admin.');
      return;
    }
    const user = { username: u, role: roleMap[u] };
    onLogin(user);
  };

  return (
    <div className="auth-wrapper">
      <div className="login-card">
        <div className="login-card-header">
          <img src={logoUrl} alt="ERP-SMB Logo" className="login-logo" />
          <div className="login-title">ERP-SMB</div>
        </div>
        <div className="login-card-body">
          <h2 className="login-heading">Sign in</h2>
          <p className="login-sub">Use role-based demo accounts</p>
          <form className="form" onSubmit={submit}>
            <div className="form-grid">
              <label>
                <span>Username</span>
                <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="admin | sales | warehouse | finance | hr" />
              </label>
              <label>
                <span>Password</span>
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Same as username" />
              </label>
            </div>
            {error && <div className="form-error" role="alert">{error}</div>}
            <div className="form-actions">
              <button className="btn btn-primary" type="submit">Login</button>
            </div>
            <div className="form-hint">Examples: admin/admin, sales/sales, warehouse/warehouse, finance/finance, hr/hr</div>
          </form>
        </div>
      </div>
    </div>
  );
}
