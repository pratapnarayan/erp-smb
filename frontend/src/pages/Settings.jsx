import React, { useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import http from '../api/clients/http.js';

export default function Settings({ theme, setTheme }) {
  const [company, setCompany] = useState({ legalName: '', taxId: '', address: '', city: '' });
  const [saveStatus, setSaveStatus] = useState('');

  const [pwForm, setPwForm] = useState({ current: '', next: '', confirm: '' });
  const [pwErrors, setPwErrors] = useState({});
  const [pwStatus, setPwStatus] = useState('');

  const user = (() => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch { return {}; } })();

  const handleSave = async (e) => {
    e.preventDefault();
    setSaveStatus('saving');
    try {
      await http.post('/settings/company', company);
      setSaveStatus('saved');
      setTimeout(() => setSaveStatus(''), 3000);
    } catch {
      setSaveStatus('error');
    }
  };

  const validatePw = () => {
    const errors = {};
    if (!pwForm.current) errors.current = 'Current password is required';
    if (!pwForm.next || pwForm.next.length < 8) errors.next = 'New password must be at least 8 characters';
    if (pwForm.next !== pwForm.confirm) errors.confirm = 'Passwords do not match';
    return errors;
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    const errors = validatePw();
    if (Object.keys(errors).length > 0) { setPwErrors(errors); return; }
    setPwErrors({});
    setPwStatus('saving');
    try {
      await http.post('/auth/change-password', { currentPassword: pwForm.current, newPassword: pwForm.next });
      setPwStatus('saved');
      setPwForm({ current: '', next: '', confirm: '' });
      setTimeout(() => setPwStatus(''), 4000);
    } catch (err) {
      const msg = err?.response?.data?.error;
      if (msg === 'wrong_password') {
        setPwErrors({ current: 'Current password is incorrect' });
      } else {
        setPwStatus('error');
      }
    }
  };

  return (
    <div className="grid cols-2">
      <FrostedCard title="Appearance" subtitle="Theme and display density">
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
        <form className="form" onSubmit={handleSave}>
          <div className="grid cols-2">
            <label style={{ display: 'grid', gap: 6 }}>
              <span>Legal Name</span>
              <input value={company.legalName} onChange={(e) => setCompany({ ...company, legalName: e.target.value })} placeholder="Your Company LLC" />
            </label>
            <label style={{ display: 'grid', gap: 6 }}>
              <span>Tax ID / GST Number</span>
              <input value={company.taxId} onChange={(e) => setCompany({ ...company, taxId: e.target.value })} placeholder="12-3456789" />
            </label>
            <label style={{ display: 'grid', gap: 6 }}>
              <span>Address</span>
              <input value={company.address} onChange={(e) => setCompany({ ...company, address: e.target.value })} placeholder="123 Main St" />
            </label>
            <label style={{ display: 'grid', gap: 6 }}>
              <span>City</span>
              <input value={company.city} onChange={(e) => setCompany({ ...company, city: e.target.value })} placeholder="Mumbai" />
            </label>
          </div>
          <div className="form-actions" style={{ marginTop: 12, gap: 10 }}>
            <button className="btn btn-primary" type="submit" disabled={saveStatus === 'saving'}>
              {saveStatus === 'saving' ? 'Saving…' : 'Save Settings'}
            </button>
            {saveStatus === 'saved' && <span style={{ color: 'var(--green-500)', fontSize: 13 }}>✓ Saved successfully</span>}
            {saveStatus === 'error' && <span style={{ color: 'var(--red-500)', fontSize: 13 }}>Failed to save — please try again</span>}
          </div>
        </form>
      </FrostedCard>

      <FrostedCard title="Profile" subtitle="Signed-in user details">
        <div className="grid cols-2" style={{ gap: 12 }}>
          <div>
            <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Username</div>
            <div style={{ fontWeight: 600, marginTop: 2 }}>{user.username || '—'}</div>
          </div>
          <div>
            <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Role</div>
            <div style={{ fontWeight: 600, marginTop: 2, textTransform: 'capitalize' }}>{user.role || '—'}</div>
          </div>
        </div>
      </FrostedCard>

      <FrostedCard title="Change Password" subtitle="Update your account password">
        <form className="form" onSubmit={handleChangePassword}>
          <div className="grid cols-1" style={{ gap: 12 }}>
            <label style={{ display: 'grid', gap: 4 }}>
              <span>Current Password <span className="required">*</span></span>
              <input
                type="password"
                value={pwForm.current}
                onChange={(e) => setPwForm({ ...pwForm, current: e.target.value })}
                placeholder="Current password"
                className={pwErrors.current ? 'input-error' : ''}
                autoComplete="current-password"
              />
              {pwErrors.current && <span className="field-error">{pwErrors.current}</span>}
            </label>
            <label style={{ display: 'grid', gap: 4 }}>
              <span>New Password <span className="required">*</span></span>
              <input
                type="password"
                value={pwForm.next}
                onChange={(e) => setPwForm({ ...pwForm, next: e.target.value })}
                placeholder="At least 8 characters"
                className={pwErrors.next ? 'input-error' : ''}
                autoComplete="new-password"
              />
              {pwErrors.next && <span className="field-error">{pwErrors.next}</span>}
            </label>
            <label style={{ display: 'grid', gap: 4 }}>
              <span>Confirm New Password <span className="required">*</span></span>
              <input
                type="password"
                value={pwForm.confirm}
                onChange={(e) => setPwForm({ ...pwForm, confirm: e.target.value })}
                placeholder="Repeat new password"
                className={pwErrors.confirm ? 'input-error' : ''}
                autoComplete="new-password"
              />
              {pwErrors.confirm && <span className="field-error">{pwErrors.confirm}</span>}
            </label>
          </div>
          <div className="form-actions" style={{ marginTop: 12, gap: 10 }}>
            <button className="btn btn-primary" type="submit" disabled={pwStatus === 'saving'}>
              {pwStatus === 'saving' ? 'Updating…' : 'Update Password'}
            </button>
            {pwStatus === 'saved' && <span style={{ color: 'var(--green-500)', fontSize: 13 }}>✓ Password changed successfully</span>}
            {pwStatus === 'error' && <span style={{ color: 'var(--red-500)', fontSize: 13 }}>Failed to update password — please try again</span>}
          </div>
        </form>
      </FrostedCard>
    </div>
  );
}
