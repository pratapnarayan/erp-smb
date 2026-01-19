import React, { useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import DataImport from '../components/DataImport.jsx';

const userColumns = [
  { key: 'email', label: 'Email' },
  { key: 'role', label: 'Role' },
  { key: 'active', label: 'Active', render: (v) => <Badge color={v ? 'green' : 'red'}>{v ? 'Yes' : 'No'}</Badge> },
];

const initialUsers = [
  { email: 'owner@smb.com', role: 'Owner', active: true },
  { email: 'ops@smb.com', role: 'Operations', active: true },
  { email: 'temp@smb.com', role: 'Viewer', active: false },
];

export default function Admin({ currentRole }) {
  const [users, setUsers] = useState(initialUsers);
  const [form, setForm] = useState({ email: '', role: 'Viewer', active: true });
  const [activeSection, setActiveSection] = useState('users');

  const addUser = (e) => {
    e.preventDefault();
    if (!form.email) return;
    setUsers([{ ...form }, ...users]);
    setForm({ email: '', role: 'Viewer', active: true });
  };

  const canInvite = currentRole === 'ROLE_ADMIN' || currentRole === 'ROLE_HR';
  const canImport = currentRole === 'ROLE_ADMIN' || currentRole === 'ROLE_OWNER';

  return (
    <div>
      {/* Section Tabs */}
      <div style={{ marginBottom: '1.5rem', display: 'flex', gap: '1rem' }}>
        <button
          onClick={() => setActiveSection('users')}
          className={activeSection === 'users' ? 'btn btn-primary' : 'btn btn-secondary'}
        >
          👥 User Management
        </button>
        {canImport && (
          <button
            onClick={() => setActiveSection('import')}
            className={activeSection === 'import' ? 'btn btn-primary' : 'btn btn-secondary'}
          >
            📊 Data Import
          </button>
        )}
      </div>

      {activeSection === 'import' && canImport ? (
        <DataImport />
      ) : (
        <div className="grid cols-1">
      {canInvite ? (
      <FrostedCard title="Invite User" subtitle="Access management">
        <form className="form" onSubmit={addUser}>
          <div className="form-grid">
            <label>
              <span>Email</span>
              <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="name@company.com" />
            </label>
            <label>
              <span>Role</span>
              <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                <option>Owner</option>
                <option>Admin</option>
                <option>Operations</option>
                <option>Finance</option>
                <option>Viewer</option>
              </select>
            </label>
            <label>
              <span>Active</span>
              <select value={String(form.active)} onChange={(e) => setForm({ ...form, active: e.target.value === 'true' })}>
                <option value="true">Yes</option>
                <option value="false">No</option>
              </select>
            </label>
          </div>
          <div className="form-actions">
            <button className="btn btn-primary" type="submit">Add User</button>
          </div>
        </form>
      </FrostedCard>
      ) : (
        <FrostedCard title="Access Restricted" subtitle="Only ADMIN or HR can add users">
          <div>You are signed in as <strong>{currentRole}</strong>. You can view users but cannot invite new users.</div>
        </FrostedCard>
      )}

      <FrostedCard title="Users" subtitle="Organization members">
        <DataTable columns={userColumns} rows={users} />
      </FrostedCard>
        </div>
      )}
    </div>
  );
}
