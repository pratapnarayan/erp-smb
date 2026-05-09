import React, { useMemo, useState } from 'react';

export default function DataTable({ columns, rows, initialSort, onRowClick }) {
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

  const clickable = typeof onRowClick === 'function';

  return (
    <div className="table-wrapper">
      <table className={`table${clickable ? ' table--clickable' : ''}`}>
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
            <tr
              key={r.id ?? i}
              onClick={clickable ? () => onRowClick(r) : undefined}
              style={clickable ? { cursor: 'pointer' } : undefined}
              className={clickable ? 'table-row--hoverable' : undefined}
            >
              {columns.map((c) => (
                <td key={c.key} onClick={c.key === 'actions' ? (e) => e.stopPropagation() : undefined}>
                  {c.render ? c.render(r[c.key], r) : r[c.key]}
                </td>
              ))}
            </tr>
          ))}
          {sorted.length === 0 && (
            <tr>
              <td colSpan={columns.length} style={{ textAlign: 'center', padding: '24px', color: 'hsl(var(--muted))' }}>
                No records found
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
