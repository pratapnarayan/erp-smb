import React, { useMemo, useState } from 'react';

export default function DataTable({ columns, rows, initialSort }) {
  const [sort, setSort] = useState(initialSort || { key: columns[0]?.key, dir: 'asc' });

  const sorted = useMemo(() => {
    const copy = [...rows];
    if (sort?.key) {
      copy.sort((a, b) => {
        const av = a[sort.key];
        const bv = b[sort.key];
        if (av == null) return 1;
        if (bv == null) return -1;
        if (typeof av === 'number' && typeof bv === 'number') {
          return sort.dir === 'asc' ? av - bv : bv - av;
        }
        return sort.dir === 'asc' ? String(av).localeCompare(String(bv)) : String(bv).localeCompare(String(av));
      });
    }
    return copy;
  }, [rows, sort]);

  return (
    <div className="table-wrapper">
      <table className="table">
        <thead>
          <tr>
            {columns.map((c) => (
              <th key={c.key}>
                <button
                  className={`th-btn ${sort.key === c.key ? 'sorted ' + sort.dir : ''}`}
                  onClick={() => setSort({ key: c.key, dir: sort.key === c.key && sort.dir === 'asc' ? 'desc' : 'asc' })}
                >
                  {c.label}
                </button>
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {sorted.map((r, i) => (
            <tr key={i}>
              {columns.map((c) => (
                <td key={c.key}>{c.render ? c.render(r[c.key], r) : r[c.key]}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
