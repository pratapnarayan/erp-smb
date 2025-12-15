import React, { useState } from 'react';
import logoUrl from '../assets/logo.svg';
import http from '../api/clients/http.js';

export default function Login({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    const u = username.trim();
    const p = password.trim();
    if (!u || !p) {
      setError('Please enter username and password.');
      return;
    }
    setLoading(true);
    try {
      const { data } = await http.post('/auth/login', { username: u, password: p });
      // Expected: { accessToken, refreshToken, username, role }
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      const user = { username: data.username, role: data.role };
      localStorage.setItem('user', JSON.stringify(user));
      onLogin(user);
    } catch (err) {
      console.error('Login failed', err);
      setError('Invalid credentials or server unavailable.');
    } finally {
      setLoading(false);
    }
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
