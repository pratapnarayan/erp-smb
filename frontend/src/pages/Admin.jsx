import React, { useEffect, useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import DataImport from '../components/DataImport.jsx';
import { authApi, usersApi } from '../api/clients/index.js';

const ROLES = ['Admin', 'Owner', 'Manager', 'HR', 'Finance', 'Operations', 'User', 'Viewer'];

const roleColor = (r) => {
  const s = String(r || '').toLowerCase();
  if (s === 'admin' || s === 'owner') return 'red';
  if (s === 'manager' || s === 'hr') return 'blue';
  if (s === 'finance' || s === 'operations') return 'amber';
  return 'gray';
};

const EMPTY_FORM = { username: '', fullName: '', password: '', role: 'User' };

// ── Inline delete-confirmation cell ─────────────────────────────────────────
function DeleteCell({ username, currentUsername, onDelete }) {
  const [confirming, setConfirming] = useState(false);
  const [deleting,   setDeleting]   = useState(false);

  if (username === currentUsername) {
    return <span style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>—</span>;
  }

  if (confirming) {
    return (
      <span style={{ display: 'flex', gap: 4, alignItems: 'center' }}>
        <button
          className="btn"
          style={{ fontSize: 11, padding: '2px 8px', background: 'hsl(0 72% 51%)', color: '#fff', border: 'none' }}
          disabled={deleting}
          onClick={async () => {
            setDeleting(true);
            await onDelete(username);
            setDeleting(false);
            setConfirming(false);
          }}
        >
          {deleting ? '...' : 'Yes, delete'}
        </button>
        <button
          className="btn btn-secondary"
          style={{ fontSize: 11, padding: '2px 8px' }}
          disabled={deleting}
          onClick={() => setConfirming(false)}
        >
          Cancel
        </button>
      </span>
    );
  }

  return (
    <button
      className="btn btn-secondary"
      style={{ fontSize: 11, padding: '2px 8px', color: 'hsl(0 65% 45%)' }}
      onClick={() => setConfirming(true)}
    >
      Delete
    </button>
  );
}

export default function Admin({ currentRole, currentUsername }) {
  const [users, setUsers]               = useState([]);
  const [usersLoading, setUsersLoading] = useState(true);
  const [usersError, setUsersError]     = useState('');

  const [form, setForm]                 = useState(EMPTY_FORM);
  const [showPassword, setShowPassword] = useState(false);
  const [formErrors, setFormErrors]     = useState({});
  const [saving, setSaving]             = useState(false);
  // saveError:   red banner   -- hard failure, nothing was created
  // saveWarn:    amber banner -- partial success (auth OK, profile failed) or partial delete
  // saveSuccess: green banner -- full success
  const [saveError, setSaveError]       = useState('');
  const [saveWarn, setSaveWarn]         = useState('');
  const [saveSuccess, setSaveSuccess]   = useState('');

  const [activeSection, setActiveSection] = useState('users');

  // ── Load users from user-service on mount ──────────────────────────────────
  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    setUsersLoading(true);
    setUsersError('');
    try {
      const data = await usersApi.list(0, 100);
      setUsers(data.content || []);
    } catch (err) {
      console.error('Failed to load users', err);
      setUsersError('Failed to load users. Please refresh.');
    } finally {
      setUsersLoading(false);
    }
  };

  // ── Form validation ─────────────────────────────────────────────────────────
  const validate = () => {
    const errors = {};
    const u = form.username.trim();
    if (!u) {
      errors.username = 'Username is required';
    } else if (u.length < 3) {
      errors.username = 'Username must be at least 3 characters';
    } else if (!/^[a-zA-Z0-9._@+\-]+$/.test(u)) {
      errors.username = 'Only letters, digits, and . _ @ + - are allowed';
    }
    if (!form.password) {
      errors.password = 'Password is required';
    } else if (form.password.length < 8) {
      errors.password = 'Password must be at least 8 characters';
    }
    if (!form.role) errors.role = 'Role is required';
    return errors;
  };

  // ── Create: orchestrate auth-service signup then user-service profile ────────
  //
  // Phase 1 - POST /auth/signup  -> creates login credentials (bcrypt-hashed)
  // Phase 2 - POST /api/users    -> creates display profile in user directory
  //
  // Partial-failure: if Phase 1 succeeds but Phase 2 fails the user can already
  // authenticate. Amber warning surfaces the partial state to the admin.
  const addUser = async (e) => {
    e.preventDefault();
    setSaveError(''); setSaveWarn(''); setSaveSuccess('');

    const errors = validate();
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }
    setFormErrors({});
    setSaving(true);

    const username = form.username.trim();
    const fullName = form.fullName.trim() || username;
    const { password, role } = form;

    // -- Phase 1: create auth credentials --------------------------------------
    try {
      await authApi.signup(username, password, role.toUpperCase());
    } catch (err) {
      setSaving(false);
      const code = err?.response?.data?.error;
      if (code === 'username_taken') {
        setFormErrors({ username: 'This username is already taken. Choose a different one.' });
      } else if (err?.response?.status === 403) {
        setSaveError('Permission denied. Only ADMIN or HR can create users.');
      } else {
        setSaveError('Failed to create login credentials. Please try again.');
      }
      return;
    }

    // -- Phase 2: create display profile ---------------------------------------
    try {
      const created = await usersApi.create({ username, fullName, role });
      setUsers((prev) => [created, ...prev]);
      setForm(EMPTY_FORM);
      setShowPassword(false);
      setSaveSuccess(`User "${created.username}" provisioned successfully. They can now log in.`);
      setTimeout(() => setSaveSuccess(''), 5000);
    } catch (err) {
      const code = err?.response?.data?.error;
      if (code === 'username_already_exists') {
        setSaveWarn(
          `Login credentials created for "${username}". ` +
          `A profile with this username already exists in the directory -- no change made there.`
        );
        await loadUsers();
        setForm(EMPTY_FORM); setShowPassword(false);
      } else if (err?.response?.status === 403) {
        setSaveWarn(
          `Login credentials for "${username}" were created, but the profile could not be saved ` +
          `(permission denied). The user can already log in. Contact an ADMIN to add the profile manually.`
        );
      } else {
        setSaveWarn(
          `Login credentials for "${username}" were created, but saving the user profile failed. ` +
          `The user can already log in. Refresh the list -- if they don't appear, contact your administrator.`
        );
      }
      console.error('Phase 2 (profile creation) failed after auth signup succeeded', err);
    } finally {
      setSaving(false);
    }
  };

  // ── Delete: orchestrate auth credential removal then profile removal ─────────
  //
  // Phase 1 - DELETE /auth/users/{username}  -> removes login credentials
  // Phase 2 - DELETE /api/users/{username}   -> removes directory profile
  //
  // Partial-failure: if Phase 1 fails we abort (nothing changed). If Phase 2
  // fails, credentials are gone but the profile row remains -- amber warning.
  const deleteUser = async (username) => {
    setSaveError(''); setSaveWarn(''); setSaveSuccess('');

    // -- Phase 1: remove auth credentials --------------------------------------
    try {
      await authApi.deleteUser(username);
    } catch (err) {
      const code = err?.response?.data?.error;
      const status = err?.response?.status;
      if (status === 404) {
        // Credentials already absent -- proceed to profile cleanup
      } else if (status === 403 || code === 'forbidden') {
        setSaveError('Permission denied. Only ADMIN can delete users.');
        return;
      } else if (code === 'self_delete_forbidden') {
        setSaveError('You cannot delete your own account.');
        return;
      } else {
        setSaveError(`Failed to remove login credentials for "${username}". No changes were made.`);
        return;
      }
    }

    // -- Phase 2: remove profile -----------------------------------------------
    try {
      await usersApi.delete(username);
      setUsers((prev) => prev.filter((u) => u.username !== username));
      setSaveSuccess(`User "${username}" has been removed.`);
      setTimeout(() => setSaveSuccess(''), 5000);
    } catch (err) {
      const status = err?.response?.status;
      if (status === 404) {
        // Profile already absent -- still show success and refresh
        setUsers((prev) => prev.filter((u) => u.username !== username));
        setSaveSuccess(`User "${username}" has been removed.`);
        setTimeout(() => setSaveSuccess(''), 5000);
      } else {
        setSaveWarn(
          `Login credentials for "${username}" were removed, but the directory profile could not be deleted. ` +
          `The user can no longer log in. Refresh the list -- the profile row may need manual cleanup.`
        );
        console.error('Phase 2 (profile delete) failed after auth credentials were removed', err);
        await loadUsers();
      }
    }
  };

  // currentRole is the bare role string from the login response (e.g. "ADMIN"),
  // not the "ROLE_"-prefixed form Spring Security uses internally for authorities.
  const role = String(currentRole || '').toUpperCase();
  const canInvite = role === 'ADMIN' || role === 'HR';
  const canDelete = role === 'ADMIN';
  const canImport = role === 'ADMIN' || role === 'OWNER';

  // Build columns dynamically so the delete column only appears for ADMIN
  const userColumns = [
    { key: 'username', label: 'Username' },
    { key: 'fullName', label: 'Full Name' },
    {
      key: 'role', label: 'Role',
      render: (v) => <Badge color={roleColor(v)}>{v}</Badge>,
    },
    ...(canDelete ? [{
      key: '_actions', label: '',
      render: (_, row) => (
        <DeleteCell
          username={row.username}
          currentUsername={currentUsername}
          onDelete={deleteUser}
        />
      ),
    }] : []),
  ];

  return (
    <div>
      {/* Section Tabs */}
      <div style={{ marginBottom: '1.5rem', display: 'flex', gap: '1rem' }}>
        <button
          onClick={() => setActiveSection('users')}
          className={activeSection === 'users' ? 'btn btn-primary' : 'btn btn-secondary'}
        >
          User Management
        </button>
        {canImport && (
          <button
            onClick={() => setActiveSection('import')}
            className={activeSection === 'import' ? 'btn btn-primary' : 'btn btn-secondary'}
          >
            Data Import
          </button>
        )}
      </div>

      {activeSection === 'import' && canImport ? (
        <DataImport />
      ) : (
        <div className="grid cols-1">

          {/* Invite form (ADMIN / HR only) */}
          {canInvite ? (
            <FrostedCard
              title="Invite User"
              subtitle="Creates login credentials and a directory profile in one step"
            >
              {saveError && (
                <div className="form-error" role="alert" style={{ marginBottom: 12 }}>
                  {saveError}
                </div>
              )}
              {saveWarn && (
                <div role="alert" style={{
                  marginBottom: 12, padding: '8px 12px', borderRadius: 8,
                  background: 'hsl(38 92% 50% / 0.1)', color: 'hsl(38 60% 28%)',
                  fontSize: 13, border: '1px solid hsl(38 92% 60% / 0.4)',
                }}>
                  {saveWarn}
                </div>
              )}
              {saveSuccess && (
                <div style={{
                  marginBottom: 12, padding: '8px 12px', borderRadius: 8,
                  background: 'hsl(142 70% 45% / 0.1)', color: 'hsl(142 60% 30%)',
                  fontSize: 13, border: '1px solid hsl(142 60% 60% / 0.3)',
                }}>
                  {saveSuccess}
                </div>
              )}

              <form className="form" onSubmit={addUser}>
                <div className="form-grid">

                  <label>
                    <span>Username <span className="required">*</span></span>
                    <input
                      value={form.username}
                      onChange={(e) => setForm({ ...form, username: e.target.value })}
                      placeholder="e.g. priya.sharma or priya@company.com"
                      autoComplete="off"
                      className={formErrors.username ? 'input-error' : ''}
                    />
                    {formErrors.username && (
                      <span className="field-error">{formErrors.username}</span>
                    )}
                  </label>

                  <label>
                    <span>Full Name</span>
                    <input
                      value={form.fullName}
                      onChange={(e) => setForm({ ...form, fullName: e.target.value })}
                      placeholder="e.g. Priya Sharma"
                      autoComplete="off"
                    />
                  </label>

                  <label>
                    <span>Password <span className="required">*</span></span>
                    <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                      <input
                        type={showPassword ? 'text' : 'password'}
                        value={form.password}
                        onChange={(e) => setForm({ ...form, password: e.target.value })}
                        placeholder="Min. 8 characters"
                        autoComplete="new-password"
                        className={formErrors.password ? 'input-error' : ''}
                        style={{ flex: 1, paddingRight: '2.5rem' }}
                      />
                      <button
                        type="button"
                        onClick={() => setShowPassword((v) => !v)}
                        style={{
                          position: 'absolute', right: 8, background: 'none', border: 'none',
                          cursor: 'pointer', fontSize: 12, color: 'hsl(var(--muted))',
                          padding: '0 2px', lineHeight: 1,
                        }}
                        aria-label={showPassword ? 'Hide password' : 'Show password'}
                        tabIndex={-1}
                      >
                        {showPassword ? 'Hide' : 'Show'}
                      </button>
                    </div>
                    {formErrors.password && (
                      <span className="field-error">{formErrors.password}</span>
                    )}
                  </label>

                  <label>
                    <span>Role <span className="required">*</span></span>
                    <select
                      value={form.role}
                      onChange={(e) => setForm({ ...form, role: e.target.value })}
                      className={formErrors.role ? 'input-error' : ''}
                    >
                      {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
                    </select>
                    {formErrors.role && (
                      <span className="field-error">{formErrors.role}</span>
                    )}
                  </label>

                </div>
                <div className="form-actions">
                  <button className="btn btn-primary" type="submit" disabled={saving}>
                    {saving ? 'Provisioning...' : 'Create User'}
                  </button>
                </div>
              </form>
            </FrostedCard>
          ) : (
            <FrostedCard title="Access Restricted" subtitle="Only ADMIN or HR can add users">
              <div>
                You are signed in as <strong>{currentRole}</strong>.
                You can view users but cannot invite new ones.
              </div>
            </FrostedCard>
          )}

          {/* User list */}
          <FrostedCard
            title="Users"
            subtitle={`Organisation members${users.length > 0 ? ` -- ${users.length} total` : ''}`}
            actions={
              <button className="btn" onClick={loadUsers} disabled={usersLoading}>
                {usersLoading ? 'Loading...' : 'Refresh'}
              </button>
            }
          >
            {/* Shared feedback banners for delete actions */}
            {saveError && !saving && (
              <div className="form-error" role="alert" style={{ margin: '0 0 12px' }}>{saveError}</div>
            )}
            {saveWarn && !saving && (
              <div role="alert" style={{
                margin: '0 0 12px', padding: '8px 12px', borderRadius: 8,
                background: 'hsl(38 92% 50% / 0.1)', color: 'hsl(38 60% 28%)',
                fontSize: 13, border: '1px solid hsl(38 92% 60% / 0.4)',
              }}>
                {saveWarn}
              </div>
            )}
            {saveSuccess && !saving && (
              <div style={{
                margin: '0 0 12px', padding: '8px 12px', borderRadius: 8,
                background: 'hsl(142 70% 45% / 0.1)', color: 'hsl(142 60% 30%)',
                fontSize: 13, border: '1px solid hsl(142 60% 60% / 0.3)',
              }}>
                {saveSuccess}
              </div>
            )}
            {usersLoading && (
              <div style={{ color: 'hsl(var(--muted))', padding: 16, fontSize: 13 }}>Loading...</div>
            )}
            {usersError && (
              <div className="form-error" role="alert" style={{ margin: '8px 0' }}>{usersError}</div>
            )}
            {!usersLoading && !usersError && users.length === 0 && (
              <div style={{ padding: '24px', textAlign: 'center', color: 'hsl(var(--muted))' }}>
                No users found.
              </div>
            )}
            {users.length > 0 && <DataTable columns={userColumns} rows={users} />}
          </FrostedCard>

        </div>
      )}
    </div>
  );
}
