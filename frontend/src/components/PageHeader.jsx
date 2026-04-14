import React from 'react';
import { Plus } from 'lucide-react';

export default function PageHeader({ title, subtitle, action, onAction, icon: Icon }) {
  return (
    <div style={styles.header}>
      <div style={styles.left}>
        {Icon && (
          <div style={styles.iconWrap}>
            <Icon size={20} color="#6366f1" />
          </div>
        )}
        <div>
          <h1 style={styles.title}>{title}</h1>
          {subtitle && <p style={styles.subtitle}>{subtitle}</p>}
        </div>
      </div>
      {action && (
        <button style={styles.btn} onClick={onAction}>
          <Plus size={15} />
          {action}
        </button>
      )}
    </div>
  );
}

const styles = {
  header: {
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    marginBottom: '24px', flexWrap: 'wrap', gap: '12px',
  },
  left: { display: 'flex', alignItems: 'center', gap: '14px' },
  iconWrap: {
    width: '46px', height: '46px', borderRadius: '12px',
    background: 'rgba(99,102,241,0.1)', border: '1px solid rgba(99,102,241,0.2)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
  },
  title: { fontSize: '22px', fontWeight: '800', color: '#f1f5f9', lineHeight: 1 },
  subtitle: { fontSize: '13px', color: '#64748b', marginTop: '4px' },
  btn: {
    display: 'flex', alignItems: 'center', gap: '6px',
    padding: '10px 18px', borderRadius: '10px',
    background: 'linear-gradient(135deg,#6366f1,#4f46e5)',
    color: '#fff', fontWeight: '600', fontSize: '13px',
    border: 'none', cursor: 'pointer',
  },
};
