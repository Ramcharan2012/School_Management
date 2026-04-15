import React, { useState, useEffect } from 'react';
import { User, Lock, Phone, Mail, Shield, Edit3, Check, X } from 'lucide-react';
import api from '../services/api';

export default function ProfilePage() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [pwMode, setPwMode] = useState(false);
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState(null);
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });

  useEffect(() => {
    api.get('/auth/me').then(r => {
      setProfile(r.data.data || r.data);
    }).catch(() => setProfile(null)).finally(() => setLoading(false));
  }, []);

  const handlePwChange = async (e) => {
    e.preventDefault();
    if (pw.newPassword !== pw.confirmPassword) {
      setMsg({ type: 'error', text: 'New passwords do not match.' });
      return;
    }
    if (pw.newPassword.length < 8) {
      setMsg({ type: 'error', text: 'Password must be at least 8 characters.' });
      return;
    }
    setSaving(true);
    try {
      await api.post('/auth/change-password', pw);
      setMsg({ type: 'success', text: 'Password changed successfully!' });
      setPw({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setPwMode(false);
    } catch (e) {
      setMsg({ type: 'error', text: e.response?.data?.message || 'Failed to change password.' });
    } finally {
      setSaving(false);
    }
  };

  const roleColor = { ADMIN: '#6366f1', TEACHER: '#06b6d4', STUDENT: '#22c55e', STAFF: '#f59e0b' };
  const role = profile?.role || 'USER';
  const color = roleColor[role] || '#6366f1';

  if (loading) return (
    <div style={s.page}>
      <div style={s.spinner} />
    </div>
  );

  return (
    <div style={s.page}>
      <div style={s.container}>

        {/* Hero Card */}
        <div style={{ ...s.heroCard, background: `linear-gradient(135deg, ${color}22, #1e293b)`, borderColor: `${color}44` }}>
          <div style={{ ...s.avatar, background: `linear-gradient(135deg, ${color}, #1e293b66)` }}>
            {profile?.fullName?.charAt(0)?.toUpperCase() || 'U'}
          </div>
          <div style={s.heroInfo}>
            <h1 style={s.heroName}>{profile?.fullName || 'User'}</h1>
            <p style={s.heroEmail}>{profile?.email || '—'}</p>
            <div style={{ ...s.roleBadge, background: `${color}20`, color, borderColor: `${color}40` }}>
              <Shield size={11} /> {role}
            </div>
          </div>
        </div>

        <div style={s.grid}>
          {/* Info Card */}
          <div style={s.card}>
            <div style={s.cardHeader}>
              <User size={16} color="#6366f1" />
              <span style={s.cardTitle}>Account Information</span>
            </div>
            <div style={s.fieldList}>
              <Field label="Full Name" value={profile?.fullName} icon={<User size={14} />} />
              <Field label="Email" value={profile?.email} icon={<Mail size={14} />} />
              <Field label="Username" value={profile?.username} icon={<User size={14} />} />
              <Field label="Phone" value={profile?.phoneNumber || 'Not set'} icon={<Phone size={14} />} />
              <Field label="Role" value={role} icon={<Shield size={14} />} />
              <Field label="Status" value={profile?.status || 'ACTIVE'} icon={<Check size={14} />} />
              <Field label="Last Login" value={profile?.lastLoginAt ? new Date(profile.lastLoginAt).toLocaleString() : 'N/A'} icon={<User size={14} />} />
            </div>
          </div>

          {/* Password Card */}
          <div style={s.card}>
            <div style={s.cardHeader}>
              <Lock size={16} color="#6366f1" />
              <span style={s.cardTitle}>Change Password</span>
              {!pwMode && (
                <button style={s.editBtn} onClick={() => { setPwMode(true); setMsg(null); }}>
                  <Edit3 size={13} /> Change
                </button>
              )}
            </div>

            {msg && (
              <div style={{ ...s.alert, background: msg.type === 'success' ? 'rgba(34,197,94,0.1)' : 'rgba(239,68,68,0.1)', borderColor: msg.type === 'success' ? '#22c55e44' : '#ef444444', color: msg.type === 'success' ? '#22c55e' : '#ef4444' }}>
                {msg.type === 'success' ? <Check size={14} /> : <X size={14} />}
                {msg.text}
              </div>
            )}

            {!pwMode ? (
              <div style={s.pwPlaceholder}>
                <Lock size={32} color="#334155" />
                <p style={{ color: '#475569', fontSize: '14px', margin: 0 }}>Click "Change" to update your password</p>
              </div>
            ) : (
              <form onSubmit={handlePwChange} style={s.form}>
                <Input label="Current Password" type="password" value={pw.currentPassword} onChange={v => setPw(p => ({ ...p, currentPassword: v }))} />
                <Input label="New Password" type="password" value={pw.newPassword} onChange={v => setPw(p => ({ ...p, newPassword: v }))} />
                <Input label="Confirm New Password" type="password" value={pw.confirmPassword} onChange={v => setPw(p => ({ ...p, confirmPassword: v }))} />
                <div style={{ display: 'flex', gap: '10px', marginTop: '8px' }}>
                  <button type="submit" disabled={saving} style={s.saveBtn}>
                    {saving ? 'Saving...' : 'Save Password'}
                  </button>
                  <button type="button" style={s.cancelBtn} onClick={() => { setPwMode(false); setMsg(null); setPw({ currentPassword: '', newPassword: '', confirmPassword: '' }); }}>
                    Cancel
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function Field({ label, value, icon }) {
  return (
    <div style={s.fieldRow}>
      <div style={s.fieldLabel}>{icon}<span>{label}</span></div>
      <div style={s.fieldValue}>{value || '—'}</div>
    </div>
  );
}

function Input({ label, type, value, onChange }) {
  return (
    <div style={{ marginBottom: '14px' }}>
      <label style={s.label}>{label}</label>
      <input type={type} value={value} onChange={e => onChange(e.target.value)} required style={s.input} />
    </div>
  );
}

const s = {
  page: { padding: '32px', minHeight: '100vh', background: '#0f172a', fontFamily: 'Inter, sans-serif' },
  container: { maxWidth: '900px', margin: '0 auto' },
  spinner: { width: '40px', height: '40px', borderRadius: '50%', border: '3px solid #334155', borderTopColor: '#6366f1', animation: 'spin 0.8s linear infinite', margin: '100px auto' },
  heroCard: {
    display: 'flex', alignItems: 'center', gap: '24px',
    padding: '28px 32px', borderRadius: '16px', border: '1px solid',
    marginBottom: '24px',
  },
  avatar: {
    width: '80px', height: '80px', borderRadius: '50%',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontSize: '32px', fontWeight: '800', color: '#fff', flexShrink: 0,
  },
  heroInfo: { flex: 1 },
  heroName: { fontSize: '26px', fontWeight: '800', color: '#f1f5f9', margin: '0 0 4px' },
  heroEmail: { fontSize: '14px', color: '#64748b', margin: '0 0 10px' },
  roleBadge: {
    display: 'inline-flex', alignItems: 'center', gap: '5px',
    padding: '4px 12px', borderRadius: '20px', fontSize: '11px',
    fontWeight: '700', border: '1px solid', letterSpacing: '0.5px',
  },
  grid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' },
  card: {
    background: '#1e293b', borderRadius: '14px', border: '1px solid #334155',
    padding: '22px', display: 'flex', flexDirection: 'column', gap: '16px',
  },
  cardHeader: { display: 'flex', alignItems: 'center', gap: '10px' },
  cardTitle: { fontSize: '15px', fontWeight: '700', color: '#f1f5f9', flex: 1 },
  editBtn: {
    display: 'flex', alignItems: 'center', gap: '5px',
    padding: '6px 12px', borderRadius: '8px',
    background: 'rgba(99,102,241,0.1)', border: '1px solid rgba(99,102,241,0.3)',
    color: '#6366f1', fontSize: '12px', fontWeight: '600', cursor: 'pointer',
  },
  fieldList: { display: 'flex', flexDirection: 'column', gap: '2px' },
  fieldRow: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    padding: '9px 0', borderBottom: '1px solid #1e293b55',
  },
  fieldLabel: { display: 'flex', alignItems: 'center', gap: '8px', color: '#64748b', fontSize: '12px' },
  fieldValue: { fontSize: '13px', fontWeight: '500', color: '#cbd5e1' },
  alert: {
    display: 'flex', alignItems: 'center', gap: '8px',
    padding: '10px 14px', borderRadius: '8px', border: '1px solid',
    fontSize: '13px', fontWeight: '500',
  },
  pwPlaceholder: {
    display: 'flex', flexDirection: 'column', alignItems: 'center',
    justifyContent: 'center', gap: '12px', padding: '40px 20px',
  },
  form: { display: 'flex', flexDirection: 'column' },
  label: { display: 'block', fontSize: '12px', fontWeight: '600', color: '#94a3b8', marginBottom: '6px' },
  input: {
    width: '100%', padding: '10px 14px', borderRadius: '8px',
    background: '#0f172a', border: '1px solid #334155', color: '#f1f5f9',
    fontSize: '13px', boxSizing: 'border-box', outline: 'none',
  },
  saveBtn: {
    flex: 1, padding: '10px', borderRadius: '8px',
    background: 'linear-gradient(135deg,#6366f1,#4f46e5)',
    color: '#fff', fontWeight: '700', fontSize: '13px',
    border: 'none', cursor: 'pointer',
  },
  cancelBtn: {
    padding: '10px 16px', borderRadius: '8px',
    background: '#334155', color: '#94a3b8', fontWeight: '600',
    fontSize: '13px', border: 'none', cursor: 'pointer',
  },
};
