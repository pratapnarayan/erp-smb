import React, { useEffect, useState } from 'react';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';
import http from '../api/clients/http.js';

const TABS = ['Directory', 'Leave', 'Attendance', 'Payroll'];

const dirCols = [
  { key: 'name', label: 'Name' },
  { key: 'role', label: 'Role' },
  { key: 'dept', label: 'Department' },
  { key: 'status', label: 'Status', render: (v) => {
    const s = String(v || '').toLowerCase();
    const color = s === 'active' ? 'green' : s === 'on_leave' ? 'amber' : 'red';
    return <Badge color={color}>{v}</Badge>;
  }},
];

const DEPARTMENTS = ['Sales', 'Finance', 'Operations', 'Engineering', 'HR', 'Management'];

const DEMO_LEAVE = [
  { id: 1, employee: 'Ananya Sharma', type: 'Annual Leave', from: '2026-05-12', to: '2026-05-14', days: 3, status: 'Approved' },
  { id: 2, employee: 'Aditya Gupta', type: 'Sick Leave', from: '2026-05-08', to: '2026-05-09', days: 2, status: 'Approved' },
  { id: 3, employee: 'Myra Verma', type: 'Annual Leave', from: '2026-05-20', to: '2026-05-22', days: 3, status: 'Pending' },
  { id: 4, employee: 'Vivaan Iyer', type: 'Work From Home', from: '2026-05-07', to: '2026-05-07', days: 1, status: 'Approved' },
  { id: 5, employee: 'Ira Reddy', type: 'Maternity Leave', from: '2026-06-01', to: '2026-08-31', days: 91, status: 'Approved' },
];

const leaveCols = [
  { key: 'employee', label: 'Employee' },
  { key: 'type', label: 'Type' },
  { key: 'from', label: 'From' },
  { key: 'to', label: 'To' },
  { key: 'days', label: 'Days' },
  { key: 'status', label: 'Status', render: (v) => (
    <Badge color={v === 'Approved' ? 'green' : v === 'Pending' ? 'amber' : 'red'}>{v}</Badge>
  )},
];

const DEMO_ATTENDANCE = [
  { id: 1, employee: 'Aarav Sharma', date: '2026-05-09', checkIn: '09:02', checkOut: '18:14', hours: '9h 12m', status: 'Present' },
  { id: 2, employee: 'Diya Gupta', date: '2026-05-09', checkIn: '09:45', checkOut: '18:30', hours: '8h 45m', status: 'Late' },
  { id: 3, employee: 'Arjun Verma', date: '2026-05-09', checkIn: '—', checkOut: '—', hours: '—', status: 'Absent' },
  { id: 4, employee: 'Ananya Sharma', date: '2026-05-09', checkIn: '—', checkOut: '—', hours: '—', status: 'On Leave' },
  { id: 5, employee: 'Ishaan Iyer', date: '2026-05-09', checkIn: '08:58', checkOut: '17:55', hours: '8h 57m', status: 'Present' },
  { id: 6, employee: 'Siya Reddy', date: '2026-05-09', checkIn: '10:00', checkOut: '19:02', hours: '9h 02m', status: 'Present' },
];

const attendCols = [
  { key: 'employee', label: 'Employee' },
  { key: 'date', label: 'Date' },
  { key: 'checkIn', label: 'Check-in' },
  { key: 'checkOut', label: 'Check-out' },
  { key: 'hours', label: 'Hours' },
  { key: 'status', label: 'Status', render: (v) => {
    const color = v === 'Present' ? 'green' : v === 'Late' ? 'amber' : 'red';
    return <Badge color={color}>{v}</Badge>;
  }},
];

const DEMO_PAYROLL = [
  { id: 1, employee: 'Aarav Sharma', role: 'Senior Engineer', gross: '₹1,20,000', deductions: '₹18,400', net: '₹1,01,600', status: 'Processed' },
  { id: 2, employee: 'Diya Gupta', role: 'HR Partner', gross: '₹75,000', deductions: '₹11,500', net: '₹63,500', status: 'Processed' },
  { id: 3, employee: 'Arjun Verma', role: 'QA Analyst', gross: '₹68,000', deductions: '₹10,420', net: '₹57,580', status: 'Processed' },
  { id: 4, employee: 'Ananya Sharma', role: 'Finance Analyst', gross: '₹82,000', deductions: '₹12,560', net: '₹69,440', status: 'On Hold' },
  { id: 5, employee: 'Ishaan Iyer', role: 'DevOps Engineer', gross: '₹95,000', deductions: '₹14,550', net: '₹80,450', status: 'Processed' },
  { id: 6, employee: 'Siya Reddy', role: 'Sales Executive', gross: '₹55,000', deductions: '₹8,430', net: '₹46,570', status: 'Pending' },
];

const payrollCols = [
  { key: 'employee', label: 'Employee' },
  { key: 'role', label: 'Role' },
  { key: 'gross', label: 'Gross' },
  { key: 'deductions', label: 'Deductions' },
  { key: 'net', label: 'Net Pay' },
  { key: 'status', label: 'Status', render: (v) => (
    <Badge color={v === 'Processed' ? 'green' : v === 'Pending' ? 'amber' : 'red'}>{v}</Badge>
  )},
];

export default function HRMS() {
  const [tab, setTab] = useState('Directory');
  const [rows, setRows] = useState([]);
  const [form, setForm] = useState({ name: '', role: '', dept: 'Sales', status: 'Active' });
  const [formErrors, setFormErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');

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

      {tab === 'Leave' && (
        <FrostedCard title="Leave Requests" subtitle="Approved and pending time-off requests">
          <DataTable columns={leaveCols} rows={DEMO_LEAVE} />
        </FrostedCard>
      )}

      {tab === 'Attendance' && (
        <FrostedCard title="Attendance" subtitle={`Today's attendance — ${new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}`}>
          <DataTable columns={attendCols} rows={DEMO_ATTENDANCE} />
        </FrostedCard>
      )}

      {tab === 'Payroll' && (
        <FrostedCard title="Payroll" subtitle="Current month payroll summary">
          <div style={{ marginBottom: 12, display: 'flex', gap: 16 }}>
            <div style={{ flex: 1, background: 'hsl(var(--primary-500) / 0.08)', borderRadius: 10, padding: '12px 16px' }}>
              <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Total Gross</div>
              <div style={{ fontSize: 20, fontWeight: 700 }}>₹4,95,000</div>
            </div>
            <div style={{ flex: 1, background: 'hsl(0 80% 55% / 0.08)', borderRadius: 10, padding: '12px 16px' }}>
              <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Total Deductions</div>
              <div style={{ fontSize: 20, fontWeight: 700 }}>₹75,860</div>
            </div>
            <div style={{ flex: 1, background: 'hsl(142 70% 45% / 0.08)', borderRadius: 10, padding: '12px 16px' }}>
              <div style={{ fontSize: 12, color: 'hsl(var(--muted))' }}>Net Payable</div>
              <div style={{ fontSize: 20, fontWeight: 700 }}>₹4,19,140</div>
            </div>
          </div>
          <DataTable columns={payrollCols} rows={DEMO_PAYROLL} />
        </FrostedCard>
      )}
    </div>
  );
}
