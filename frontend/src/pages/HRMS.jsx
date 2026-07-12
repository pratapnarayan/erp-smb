import React, { useEffect, useState, useCallback } from 'react';
import { formatINR } from '../utils/formatCurrency.js';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import http from '../api/clients/http.js';

const TABS = ['Directory', 'Leave', 'Attendance', 'Payroll'];

// ── Column Definitions ────────────────────────────────────────────────────────

const dirCols = [
  { key: 'name', label: 'Name' },
  { key: 'role', label: 'Role' },
  { key: 'dept', label: 'Department' },
  {
    key: 'status', label: 'Status', render: (v) => {
      const s = String(v || '').toLowerCase();
      const color = s === 'active' ? 'green' : s === 'on_leave' ? 'amber' : 'red';
      return <Badge color={color}>{v}</Badge>;
    },
  },
];

const leaveCols = [
  { key: 'employee', label: 'Employee' },
  { key: 'type', label: 'Type' },
  { key: 'from', label: 'From' },
  { key: 'to', label: 'To' },
  { key: 'days', label: 'Days' },
  {
    key: 'status', label: 'Status', render: (v) => (
      <Badge color={v === 'Approved' ? 'green' : v === 'Pending' ? 'amber' : 'red'}>{v}</Badge>
    ),
  },
];

const attendCols = [
  { key: 'employee', label: 'Employee' },
  { key: 'date', label: 'Date' },
  { key: 'checkIn', label: 'Check-in' },
  { key: 'checkOut', label: 'Check-out' },
  { key: 'hours', label: 'Hours' },
  {
    key: 'status', label: 'Status', render: (v) => {
      const color = v === 'Present' ? 'green' : v === 'Late' ? 'amber' : 'red';
      return <Badge color={color}>{v}</Badge>;
    },
  },
];

const payrollCols = [
  { key: 'employee', label: 'Employee' },
  { key: 'role', label: 'Role' },
  { key: 'gross', label: 'Gross', render: (v) => formatINR(v) },
  { key: 'deductions', label: 'Deductions', render: (v) => formatINR(v) },
  { key: 'net', label: 'Net Pay', render: (v) => formatINR(v) },
  {
    key: 'status', label: 'Status', render: (v) => (
      <Badge color={v === 'Processed' ? 'green' : v === 'Pending' ? 'amber' : 'red'}>{v}</Badge>
    ),
  },
];

const DEPARTMENTS = ['Sales', 'Finance', 'Operations', 'Engineering', 'HR', 'Management'];

// ── Component ─────────────────────────────────────────────────────────────────

export default function HRMS() {
  const [tab, setTab] = useState('Directory');

  // Directory
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({ name: '', role: '', dept: 'Sales', status: 'Active' });
  const [formErrors, setFormErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

  // Leave
  const [leaveRows, setLeaveRows] = useState([]);
  const [leaveLoading, setLeaveLoading] = useState(false);
  const [leaveError, setLeaveError] = useState('');
  const [leaveFetched, setLeaveFetched] = useState(false);

  // Attendance
  const [attendRows, setAttendRows] = useState([]);
  const [attendLoading, setAttendLoading] = useState(false);
  const [attendError, setAttendError] = useState('');
  const [attendFetched, setAttendFetched] = useState(false);

  // Payroll
  const [payrollRows, setPayrollRows] = useState([]);
  const [payrollLoading, setPayrollLoading] = useState(false);
  const [payrollError, setPayrollError] = useState('');
  const [payrollFetched, setPayrollFetched] = useState(false);

  // ── Load functions ──────────────────────────────────────────────────────────

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const { data } = await http.get('/hrms', { params: { page: 0, size: 100 } });
        setRows(data.content || data || []);
      } catch (err) {
        console.error('Failed to load employees', err);
        setError('Failed to load employee directory.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const loadLeave = useCallback(async () => {
    setLeaveLoading(true);
    setLeaveError('');
    try {
      const { data } = await http.get('/hrms/leave', { params: { page: 0, size: 50 } });
      setLeaveRows(data.content || []);
      setLeaveFetched(true);
    } catch (err) {
      console.error('Failed to load leave requests', err);
      setLeaveError('Failed to load leave requests.');
    } finally {
      setLeaveLoading(false);
    }
  }, []);

  const loadAttendance = useCallback(async () => {
    setAttendLoading(true);
    setAttendError('');
    try {
      const { data } = await http.get('/hrms/attendance', { params: { page: 0, size: 50 } });
      setAttendRows(data.content || []);
      setAttendFetched(true);
    } catch (err) {
      console.error('Failed to load attendance', err);
      setAttendError('Failed to load attendance records.');
    } finally {
      setAttendLoading(false);
    }
  }, []);

  const loadPayroll = useCallback(async () => {
    setPayrollLoading(true);
    setPayrollError('');
    try {
      const { data } = await http.get('/hrms/payroll', { params: { page: 0, size: 50 } });
      setPayrollRows(data.content || []);
      setPayrollFetched(true);
    } catch (err) {
      console.error('Failed to load payroll', err);
      setPayrollError('Failed to load payroll data.');
    } finally {
      setPayrollLoading(false);
    }
  }, []);

  // Lazy-load tab data on first visit
  useEffect(() => {
    if (tab === 'Leave'      && !leaveFetched)   loadLeave();
    if (tab === 'Attendance' && !attendFetched)  loadAttendance();
    if (tab === 'Payroll'    && !payrollFetched) loadPayroll();
  }, [tab, leaveFetched, attendFetched, payrollFetched, loadLeave, loadAttendance, loadPayroll]);

  // ── Directory helpers ───────────────────────────────────────────────────────

  const validate = () => {
    const errors = {};
    if (!form.name.trim()) errors.name = 'Full name is required';
    if (!form.role.trim()) errors.role = 'Job title / role is required';
    return errors;
  };

  const addRow = async (e) => {
    e.preventDefault();
    const errors = validate();
    if (Object.keys(errors).length > 0) { setFormErrors(errors); return; }
    setFormErrors({});
    const { data } = await http.post('/hrms', {
      name: form.name.trim(),
      role: form.role.trim(),
      dept: form.dept,
      status: form.status,
    });
    setRows([data, ...rows]);
    setForm({ name: '', role: '', dept: 'Sales', status: 'Active' });
  };

  const filtered = rows.filter((r) =>
    !search ||
    String(r.name || '').toLowerCase().includes(search.toLowerCase()) ||
    String(r.dept || '').toLowerCase().includes(search.toLowerCase()) ||
    String(r.role || '').toLowerCase().includes(search.toLowerCase())
  );

  // ── Payroll summary ─────────────────────────────────────────────────────────

  const payrollSummary = React.useMemo(() => {
    return payrollRows.reduce(
      (acc, r) => ({
        gross:      acc.gross      + Number(r.gross      || 0),
        deductions: acc.deductions + Number(r.deductions || 0),
        net:        acc.net        + Number(r.net        || 0),
      }),
      { gross: 0, deductions: 0, net: 0 }
    );
  }, [payrollRows]);

  // ── Render ──────────────────────────────────────────────────────────────────

  return (
    <div className="grid cols-1">
      {/* Tab bar */}
      <div style={{ display: 'flex', gap: 6, borderBottom: '1px solid hsl(var(--border))', paddingBottom: 0 }}>
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            style={{
              padding: '8px 16px', border: 'none', background: 'none', cursor: 'pointer',
              color: tab === t ? 'hsl(var(--primary-500))' : 'hsl(var(--muted))',
              borderBottom: tab === t ? '2px solid hsl(var(--primary-500))' : '2px solid transparent',
              fontWeight: tab === t ? 600 : 400, fontSize: 14,
            }}
          >
            {t}
          </button>
        ))}
      </div>

      {/* ── Directory ── */}
      {tab === 'Directory' && (
        <>
          <FrostedCard title="Add Employee" subtitle="HR management">
            <form className="form" onSubmit={addRow}>
              <div className="form-grid">
                <label>
                  <span>Name <span className="required">*</span></span>
                  <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Full name" className={formErrors.name ? 'input-error' : ''} />
                  {formErrors.name && <span className="field-error">{formErrors.name}</span>}
                </label>
                <label>
                  <span>Role / Job Title <span className="required">*</span></span>
                  <input value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })} placeholder="e.g. Sales Manager" className={formErrors.role ? 'input-error' : ''} />
                  {formErrors.role && <span className="field-error">{formErrors.role}</span>}
                </label>
                <label>
                  <span>Department</span>
                  <select value={form.dept} onChange={(e) => setForm({ ...form, dept: e.target.value })}>
                    {DEPARTMENTS.map((d) => <option key={d}>{d}</option>)}
                  </select>
                </label>
                <label>
                  <span>Status</span>
                  <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                    <option>Active</option>
                    <option>Inactive</option>
                  </select>
                </label>
              </div>
              <div className="form-actions">
                <button className="btn btn-primary" type="submit">Add Employee</button>
              </div>
            </form>
          </FrostedCard>

          <FrostedCard
            title="Employees"
            subtitle="Directory"
            actions={
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search by name, role, dept…"
                style={{ padding: '6px 10px', borderRadius: 8, border: '1px solid hsl(var(--border))', background: 'hsl(var(--bg-elev))', color: 'inherit', width: 200 }}
              />
            }
          >
            {loading && <div style={{ color: 'hsl(var(--muted))', padding: 16, fontSize: 13 }}>Loading…</div>}
            {error && <div className="form-error" role="alert" style={{ margin: '8px 0' }}>{error}</div>}
            {!loading && !error && rows.length === 0 && (
              <div style={{ padding: '32px', textAlign: 'center', color: 'hsl(var(--muted))' }}>
                <div style={{ fontSize: 32, marginBottom: 8 }}>👥</div>
                <div style={{ fontWeight: 600, marginBottom: 4 }}>No employees yet</div>
                <div style={{ fontSize: 13 }}>Add your first employee using the form above.</div>
              </div>
            )}
            {filtered.length > 0 && <DataTable columns={dirCols} rows={filtered} />}
          </FrostedCard>
        </>
      )}

      {/* ── Leave ── */}
      {tab === 'Leave' && (
        <FrostedCard
          title="Leave Requests"
          subtitle="Approved and pending time-off requests"
          actions={
            <button className="btn" onClick={loadLeave} disabled={leaveLoading}>
              {leaveLoading ? 'Loading…' : '↻ Refresh'}
            </button>
          }
        >
          {leaveLoading && <div style={{ color: 'hsl(var(--muted))', padding: 16, fontSize: 13 }}>Loading…</div>}
          {leaveError && <div className="form-error" role="alert" style={{ margin: '8px 0' }}>{leaveError}</div>}
          {!leaveLoading && !leaveError && leaveRows.length === 0 && (
            <div style={{ padding: '24px', textAlign: 'center', color: 'hsl(var(--muted))' }}>No leave requests found.</div>
          )}
          {leaveRows.length > 0 && <DataTable columns={leaveCols} rows={leaveRows} />}
        </FrostedCard>
      )}

      {/* ── Attendance ── */}
      {tab === 'Attendance' && (
        <FrostedCard
          title="Attendance"
          subtitle={`Today's attendance — ${new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}`}
          actions={
            <button className="btn" onClick={loadAttendance} disabled={attendLoading}>
              {attendLoading ? 'Loading…' : '↻ Refresh'}
            </button>
          }
        >
          {attendLoading && <div style={{ color: 'hsl(var(--muted))', padding: 16, fontSize: 13 }}>Loading…</div>}
          {attendError && <div className="form-error" role="alert" style={{ margin: '8px 0' }}>{attendError}</div>}
          {!attendLoading && !attendError && attendRows.length === 0 && (
            <div style={{ padding: '24px', textAlign: 'center', color: 'hsl(var(--muted))' }}>No attendance records found for today.</div>
          )}
          {attendRows.length > 0 && <DataTable columns={attendCols} rows={attendRows} />}
        </FrostedCard>
      )}

      {/* ── Payroll ── */}
      {tab === 'Payroll' && (
        <FrostedCard
          title="Payroll"
          subtitle="Current month payroll summary"
          actions={
            <button className="btn" onClick={loadPayroll} disabled={payrollLoading}>
              {payrollLoading ? 'Loading…' : '↻ Refresh'}
            </button>
          }
        >
          {payrollLoading && <div style={{ color: 'hsl(var(--muted))', padding: 16, fontSize: 13 }}>Loading…</div>}
          {payrollError && <div className="form-error" role="alert" style={{ margin: '8px 0' }}>{payrollError}</div>}
          {payrollRows.length > 0 && (
            <>
              <div style={{ marginBottom: 12, display: 'flex', gap: 16 }}>
                <div style={{ flex: 1, background: 'hsl(var(--primary-500) / 0.08)', borderRadius: 10, padding: '12px 16px' }}>
                  <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Total Gross</div>
                  <div style={{ fontSize: 20, fontWeight: 700 }}>{formatINR(payrollSummary.gross)}</div>
                </div>
                <div style={{ flex: 1, background: 'hsl(0 80% 55% / 0.08)', borderRadius: 10, padding: '12px 16px' }}>
                  <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Total Deductions</div>
                  <div style={{ fontSize: 20, fontWeight: 700 }}>{formatINR(payrollSummary.deductions)}</div>
                </div>
                <div style={{ flex: 1, background: 'hsl(142 70% 45% / 0.08)', borderRadius: 10, padding: '12px 16px' }}>
                  <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Net Payable</div>
                  <div style={{ fontSize: 20, fontWeight: 700 }}>{formatINR(payrollSummary.net)}</div>
                </div>
              </div>
              <DataTable columns={payrollCols} rows={payrollRows} />
            </>
          )}
          {!payrollLoading && !payrollError && payrollRows.length === 0 && (
            <div style={{ padding: '24px', textAlign: 'center', color: 'hsl(var(--muted))' }}>No payroll records found.</div>
          )}
        </FrostedCard>
      )}
    </div>
  );
}
