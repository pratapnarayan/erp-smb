import React, { useState } from 'react';
import logoUrl from '../assets/logo.svg';
import http from '../api/clients/http.js';

export default function Login({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError]       = useState('');
  const [loading, setLoading]   = useState(false);

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
      // Server response body: { username, role }
      // Access and refresh tokens are set as HttpOnly cookies by the server.
      // They are NOT accessible from JavaScript — this is intentional (XSS mitigation).
      const { data } = await http.post('/auth/login', { username: u, password: p });

      // Store only non-sensitive user metadata for UI personalisation.
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
          <p className="login-sub">Sign in to your account</p>
          <form className="form" onSubmit={submit}>
            <div className="form-grid">
              <label>
                <span>Username</span>
                <input
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  placeholder="Username"
                  autoComplete="username"
                />
              </label>
              <label>
                <span>Password</span>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Password"
                  autoComplete="current-password"
                />
              </label>
            </div>
            {error && <div className="form-error" role="alert">{error}</div>}
            <div className="form-actions">
              <button className="btn btn-primary" type="submit" disabled={loading}>
                {loading ? 'Signing in…' : 'Login'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
