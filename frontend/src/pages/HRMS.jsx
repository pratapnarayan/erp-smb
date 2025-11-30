import React, { useEffect, useState } from 'react';
import { hrmsApi } from '../api/client.js';
import FrostedCard from '../components/FrostedCard.jsx';
import DataTable from '../components/DataTable.jsx';
import Badge from '../components/Badge.jsx';

const columns = [
  { key: 'name', label: 'Name' },
  { key: 'role', label: 'Role' },
  { key: 'dept', label: 'Department' },
  { key: 'status', label: 'Status', render: (v) => <Badge color={v === 'Active' ? 'green' : 'red'}>{v}</Badge> },
];

const initialData = [
  { name: 'Jane Doe', role: 'Accountant', dept: 'Finance', status: 'Active' },
  { name: 'John Smith', role: 'Warehouse Lead', dept: 'Operations', status: 'Active' },
  { name: 'Ava Chen', role: 'Sales Rep', dept: 'Sales', status: 'Inactive' },
];

export default function HRMS() {
  const [rows, setRows] = useState(initialData);
  useEffect(()=>{
    hrmsApi.list(0,20).then(p=>{ setRows(p.content.map(e=>({ name:e.name, role:e.role, dept:e.dept, status:e.status }))); });
  },[]);
  const [form, setForm] = useState({ name: '', role: '', dept: 'Sales', status: 'Active' });

  const addRow = async (e) => {
    e.preventDefault();
    if (!form.name || !form.role) return;
    setRows([{ ...form }, ...rows]);
    setForm({ name: '', role: '', dept: 'Sales', status: 'Active' });
  };

  return (
    <div className="grid cols-1">
      <FrostedCard title="Add Employee" subtitle="HR management">
        <form className="form" onSubmit={addRow}>
          <div className="form-grid">
            <label>
              <span>Name</span>
              <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder="Full name" />
            </label>
            <label>
              <span>Role</span>
              <input value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })} placeholder="Job title" />
            </label>
            <label>
              <span>Department</span>
              <select value={form.dept} onChange={(e) => setForm({ ...form, dept: e.target.value })}>
                <option>Sales</option>
                <option>Finance</option>
                <option>Operations</option>
                <option>Engineering</option>
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

      <FrostedCard title="Employees" subtitle="Directory">
        <DataTable columns={columns} rows={rows} />
      </FrostedCard>
    </div>
  );
}
