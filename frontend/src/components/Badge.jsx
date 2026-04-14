import React from 'react';

export default function Badge({ status }) {
  const map = {
    ACTIVE:    { bg: 'rgba(34,197,94,0.12)',  border: 'rgba(34,197,94,0.3)',  color: '#22c55e' },
    INACTIVE:  { bg: 'rgba(100,116,139,0.12)',border: 'rgba(100,116,139,0.3)',color: '#94a3b8' },
    APPROVED:  { bg: 'rgba(34,197,94,0.12)',  border: 'rgba(34,197,94,0.3)',  color: '#22c55e' },
    PENDING:   { bg: 'rgba(245,158,11,0.12)', border: 'rgba(245,158,11,0.3)', color: '#f59e0b' },
    REJECTED:  { bg: 'rgba(239,68,68,0.12)',  border: 'rgba(239,68,68,0.3)',  color: '#ef4444' },
    PRESENT:   { bg: 'rgba(34,197,94,0.12)',  border: 'rgba(34,197,94,0.3)',  color: '#22c55e' },
    ABSENT:    { bg: 'rgba(239,68,68,0.12)',  border: 'rgba(239,68,68,0.3)',  color: '#ef4444' },
    LATE:      { bg: 'rgba(245,158,11,0.12)', border: 'rgba(245,158,11,0.3)', color: '#f59e0b' },
    PAID:      { bg: 'rgba(34,197,94,0.12)',  border: 'rgba(34,197,94,0.3)',  color: '#22c55e' },
    UNPAID:    { bg: 'rgba(239,68,68,0.12)',  border: 'rgba(239,68,68,0.3)',  color: '#ef4444' },
    PARTIAL:   { bg: 'rgba(245,158,11,0.12)', border: 'rgba(245,158,11,0.3)', color: '#f59e0b' },
    GENERAL:   { bg: 'rgba(99,102,241,0.12)', border: 'rgba(99,102,241,0.3)', color: '#818cf8' },
    URGENT:    { bg: 'rgba(239,68,68,0.12)',  border: 'rgba(239,68,68,0.3)',  color: '#ef4444' },
    PUBLISHED: { bg: 'rgba(34,197,94,0.12)',  border: 'rgba(34,197,94,0.3)',  color: '#22c55e' },
    DRAFT:     { bg: 'rgba(100,116,139,0.12)',border: 'rgba(100,116,139,0.3)',color: '#94a3b8' },
    MALE:      { bg: 'rgba(6,182,212,0.12)',  border: 'rgba(6,182,212,0.3)',  color: '#06b6d4' },
    FEMALE:    { bg: 'rgba(236,72,153,0.12)', border: 'rgba(236,72,153,0.3)', color: '#ec4899' },
  };
  const style = map[status?.toUpperCase()] || map.INACTIVE;
  return (
    <span style={{
      padding: '3px 10px', borderRadius: '20px', fontSize: '11px',
      fontWeight: '600', letterSpacing: '0.3px', whiteSpace: 'nowrap',
      background: style.bg, border: `1px solid ${style.border}`, color: style.color,
    }}>
      {status}
    </span>
  );
}
