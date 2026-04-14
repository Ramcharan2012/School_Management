import React, { useState } from 'react';
import { Search, ChevronLeft, ChevronRight, Loader2 } from 'lucide-react';

export default function DataTable({
  columns,        // [{ key, label, render }]
  data,           // array of row objects
  loading,
  onSearch,       // optional — controlled search
  searchPlaceholder = 'Search...',
  page = 0,
  totalPages = 1,
  onPageChange,   // optional
  emptyMessage = 'No data found.',
}) {
  const [query, setQuery] = useState('');

  const handleSearch = (val) => {
    setQuery(val);
    if (onSearch) onSearch(val);
  };

  return (
    <div style={styles.wrap}>
      {/* Search Bar */}
      {onSearch && (
        <div style={styles.searchWrap}>
          <Search size={15} color="#64748b" style={styles.searchIcon} />
          <input
            style={styles.searchInput}
            placeholder={searchPlaceholder}
            value={query}
            onChange={e => handleSearch(e.target.value)}
          />
        </div>
      )}

      {/* Table */}
      <div style={styles.tableWrap}>
        <table style={styles.table}>
          <thead>
            <tr>
              {columns.map(col => (
                <th key={col.key} style={styles.th}>{col.label}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td colSpan={columns.length} style={styles.loadingCell}>
                  <Loader2 size={20} color="#6366f1" style={{ animation: 'spin 1s linear infinite' }} />
                  <span style={{ marginLeft: '8px', color: '#64748b' }}>Loading...</span>
                </td>
              </tr>
            ) : data.length === 0 ? (
              <tr>
                <td colSpan={columns.length} style={styles.emptyCell}>{emptyMessage}</td>
              </tr>
            ) : (
              data.map((row, i) => (
                <tr key={row.id ?? i} style={styles.tr}>
                  {columns.map(col => (
                    <td key={col.key} style={styles.td}>
                      {col.render ? col.render(row) : (row[col.key] ?? '—')}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {totalPages > 1 && onPageChange && (
        <div style={styles.pagination}>
          <button
            style={{ ...styles.pageBtn, ...(page === 0 ? styles.pageBtnDisabled : {}) }}
            onClick={() => page > 0 && onPageChange(page - 1)}
            disabled={page === 0}
          >
            <ChevronLeft size={15} />
          </button>
          <span style={styles.pageInfo}>Page {page + 1} of {totalPages}</span>
          <button
            style={{ ...styles.pageBtn, ...(page >= totalPages - 1 ? styles.pageBtnDisabled : {}) }}
            onClick={() => page < totalPages - 1 && onPageChange(page + 1)}
            disabled={page >= totalPages - 1}
          >
            <ChevronRight size={15} />
          </button>
        </div>
      )}

      <style>{`@keyframes spin { from{transform:rotate(0deg)} to{transform:rotate(360deg)} }`}</style>
    </div>
  );
}

const styles = {
  wrap: { display: 'flex', flexDirection: 'column', gap: '0', background: '#1e293b', borderRadius: '14px', border: '1px solid #334155', overflow: 'hidden' },
  searchWrap: { position: 'relative', padding: '16px', borderBottom: '1px solid #334155' },
  searchIcon: { position: 'absolute', left: '28px', top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none' },
  searchInput: { width: '100%', padding: '9px 14px 9px 36px', background: '#0f172a', border: '1px solid #334155', borderRadius: '8px', color: '#f1f5f9', fontSize: '14px', outline: 'none' },
  tableWrap: { overflowX: 'auto' },
  table: { width: '100%', borderCollapse: 'collapse' },
  th: { padding: '12px 16px', textAlign: 'left', fontSize: '11px', fontWeight: '600', color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.5px', background: '#16213a', borderBottom: '1px solid #334155', whiteSpace: 'nowrap' },
  tr: { borderBottom: '1px solid #1e2d3d', transition: 'background 0.1s' },
  td: { padding: '12px 16px', fontSize: '13px', color: '#cbd5e1', whiteSpace: 'nowrap' },
  loadingCell: { padding: '40px', textAlign: 'center', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' },
  emptyCell: { padding: '40px', textAlign: 'center', color: '#64748b', fontSize: '14px' },
  pagination: { display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', padding: '14px', borderTop: '1px solid #334155' },
  pageBtn: { background: '#263348', border: '1px solid #334155', borderRadius: '8px', padding: '6px 10px', cursor: 'pointer', color: '#94a3b8', display: 'flex', alignItems: 'center' },
  pageBtnDisabled: { opacity: 0.4, cursor: 'not-allowed' },
  pageInfo: { fontSize: '13px', color: '#64748b' },
};
