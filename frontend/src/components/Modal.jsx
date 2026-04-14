import React from 'react';
import { X } from 'lucide-react';

export default function Modal({ title, onClose, children, width = '480px' }) {
  return (
    <div style={styles.overlay} onClick={onClose}>
      <div style={{ ...styles.modal, width }} onClick={e => e.stopPropagation()}>
        {/* Header */}
        <div style={styles.header}>
          <h2 style={styles.title}>{title}</h2>
          <button style={styles.closeBtn} onClick={onClose}>
            <X size={18} color="#64748b" />
          </button>
        </div>
        {/* Body */}
        <div style={styles.body}>{children}</div>
      </div>
    </div>
  );
}

// Reusable form field inside modal
export function Field({ label, children }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      <label style={{ fontSize: '13px', fontWeight: '500', color: '#94a3b8' }}>{label}</label>
      {children}
    </div>
  );
}

// Reusable input style
export const inputStyle = {
  padding: '10px 14px', background: '#0f172a',
  border: '1px solid #334155', borderRadius: '8px',
  color: '#f1f5f9', fontSize: '14px', outline: 'none', width: '100%',
};

// Reusable select style
export const selectStyle = {
  ...{
    padding: '10px 14px', background: '#0f172a',
    border: '1px solid #334155', borderRadius: '8px',
    color: '#f1f5f9', fontSize: '14px', outline: 'none', width: '100%',
  }
};

const styles = {
  overlay: {
    position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)',
    backdropFilter: 'blur(4px)', display: 'flex',
    alignItems: 'center', justifyContent: 'center', zIndex: 9999, padding: '20px',
  },
  modal: {
    background: '#1e293b', borderRadius: '16px', border: '1px solid #334155',
    boxShadow: '0 25px 60px rgba(0,0,0,0.5)', maxHeight: '90vh',
    overflowY: 'auto', maxWidth: '95vw',
  },
  header: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '20px 24px', borderBottom: '1px solid #334155',
  },
  title: { fontSize: '16px', fontWeight: '700', color: '#f1f5f9' },
  closeBtn: {
    background: 'none', border: 'none', cursor: 'pointer',
    display: 'flex', padding: '4px',
  },
  body: { padding: '24px' },
};
