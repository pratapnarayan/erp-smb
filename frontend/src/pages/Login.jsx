import React, { useState } from 'react';
import { authApi } from '../api/client.js';
import logoUrl from '../assets/logo.svg';


export default function Login({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    try {
      const resp = await authApi.login(username, password);
      localStorage.setItem('accessToken', resp.accessToken);
      localStorage.setItem('refreshToken', resp.refreshToken);
      onLogin({ username: resp.username, role: resp.role });
      setError('');
    } catch (err) {
      setError('Invalid credentials');
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
          <p className="login-sub">Sign in with your credentials</p>
          <form className="form" onSubmit={submit}>
            <div className="form-grid">
              <label>
                <span>Username</span>
                <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Username" />
              </label>
              <label>
                <span>Password</span>
                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Password" />
              </label>
            </div>
            {error && <div className="form-error" role="alert">{error}</div>}
            <div className="form-actions">
              <button className="btn btn-primary" type="submit">Login</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
